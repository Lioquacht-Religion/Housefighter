abstract class Fighter{
  final boolean RIGHT = false;
  final boolean LEFT = true;
  
  PImage[] Sprs;
  
  ControlDevice device; ControlButton LightBut, MediumBut, HeavyBut, RCBut; ControlHat DPad; ControlSlider LStick_X, LStick_Y; boolean[] pInputs = new boolean[4];
  boolean[] inputs = new boolean[8]; //up , down, right, left, Roof, Mid, Base, utility(RC);
  boolean[] firstPressInp = new boolean[inputs.length];
  boolean[] firstPressDia = {true, true, true, true};
  int[] inputChargeT = new int[inputs.length];
  char[] charinputs;
  
  boolean AI_Controlled = false;
  boolean tint = false;
  
  IntList inputbufferDir = new IntList();
  ArrayList<IntList> inputbufferBut = new ArrayList<IntList>();
  int curInputTimer = 0;
  
  int comboCount = 0;
  
  int[] FBmotion = {2, 3, 6};
  int[] FBendBut = {0, 0, 10};
  int[] DPmotion = {6, 2, 3}; //{6, 3, 2, 3, 6};
  int[] fDashmotion = {6, 6};
  int[] bDashmotion = {4, 4};
  
  float x;
  float y;
  PVector Force = new PVector(0, 0);
  float gforce = 0;
  int dirMult = 1;
  float m = 1;
  
  ColRect CollisionBox;
  
  boolean facing = false;
  boolean grounded = false;
  boolean crouching = false;
  
  int maxHP = 200;
  int curHP = maxHP;
  int maxSuper = 1000;
  int curSuper = 0;
  int maxAirActions = 1;
  int curAirActions = maxAirActions;
  
  int HitStunT = 0;
  int cancelWindow = 0;
  
  ArrayList<ColCircle> HurtBoxes = new ArrayList<ColCircle>();
  ArrayList<ColCircle> HitBoxes = new ArrayList<ColCircle>();
  
  ArrayList<Projectile> Projectiles = new ArrayList<Projectile>();
  
  float[] AI_InputsScore = {.1, .1, .2, .2, .8, .3, .3};
  int pHP = 200, pOppHP = 200; float abstand = 100;
  
  Animation CurAnimation = null;
  
  Action CurAction = new Action();
  
  Action Standing, Crouching;
  Action Knockdown, softKD;
  Action BeingGrapped;
  Action InHitStun;
  Animation HHit, LHit;
  Action Jumping, fDiaJump, bDiaJump, Jumping2, fDiaJump2, bDiaJump2;
  Action Blocking;
  Animation HBlock, LBlock;
  
Action fWalk, bWalk;

Action FDash, BDash;

Action EditAction;

  Action LightNormal, MidNormal2, HeavyNormal, cr_LightNormal, cr_MidNormal, cr_HeavyNormal, j_LightNormal, j_MidNormal, j_HeavyNormal;

Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, FDash, BDash, Jumping, fDiaJump, bDiaJump},
  {cr_LightNormal, cr_MidNormal, cr_HeavyNormal},
  {j_LightNormal, j_MidNormal, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2},
  {}
};

  ArrayList<ArrayList<Action>> ActionList = new ArrayList<ArrayList<Action>>();
  ArrayList<Action> st_Actions, cr_Actions, j_Actions, sp_Actions;
  
  void fillActionsList(Action[][] ActTab){
    for(int i = 0; i < ActTab.length; i++){
      for(int j = 0; j < ActTab[i].length; j++){
        ActionList.get(i).set(j, ActTab[i][j]);
      }
    }
    
  }
  
  void AIControll2(Fighter Opp){
    float aggroLevel = dist(x, y, Opp.x, Opp.y)*0.1 + (Opp.maxHP/(Opp.curHP+1)) - (curHP/(maxHP+1));
    float attChance, moveChance = dist(x, y, Opp.x, Opp.y);
    //Attproperties needed
    int condAttKind = CurAction.MID;
    int condAttTime;
    
    if(whichDirHold(Opp, -1) || Opp.CurAction == Opp.Blocking ){  //Check Enemy Blocking
      if(Opp.inputs[1]){
        condAttKind = CurAction.HIGH;
        inputs[1] = false;
        AI_InputsScore[0] += 0.1;
      }
      else{ condAttKind = CurAction.LOW; inputs[1] = true; AI_InputsScore[1] += 0.1;}
      
    }
    
    if(aggroLevel > 70){
      for(int i = 4; i < 7; i++){
        if(moveChance <= 120 * (i-3)){
          AI_InputsScore[i] += 0.1;
        }
      }
    }
    
    if(aggroLevel > 50 && moveChance > 250){
      inputs[horDir(1)] = true;
      inputs[1] = false;
      AI_InputsScore[1] -= 0.05;
      AI_InputsScore[0] += 0.1;
      AI_InputsScore[horDir(1)] += 0.1;
      AI_InputsScore[horDir(-1)] -= 0.05;
    }
    else{      inputs[horDir(-1)] = true;
      AI_InputsScore[horDir(-1)] += 0.1;  AI_InputsScore[horDir(1)] -= 0.05;}
    
    //search for fitting and for state usable attack
          if((CurAction == Standing || CurAction == bWalk|| CurAction == fWalk || (cancelWindow > 0 && !inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){   
            if(moveChance > 600 && aggroLevel > 60 && FDash != null){ changeAction(FDash);}
       }
       else if( (CurAction == Crouching || (cancelWindow > 0 && inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){
    
         
       }
       else if( (CurAction == Standing || cancelWindow > 0) && y < GROUNDHEIGHT && !CollisionBox.bottom){
         if(moveChance > 600 && aggroLevel > 60 && FDash != null){ changeAction(FDash);}
       }
       
   // when in Hitstun
   if(CurAction == InHitStun){
     inputs[horDir(-1)] = true;
     AI_InputsScore[horDir(-1)] += 0.1;
     if(Opp.CurAction.attKind == CurAction.LOW){
       inputs[1] = true;
       AI_InputsScore[1] += 0.1;
     }
     else if(Opp.CurAction.attKind == CurAction.HIGH || Opp.CurAction.attKind == CurAction.AIR){
              inputs[1] = false;
       AI_InputsScore[1] -= 0.1;
     }
   }
   
     
  }
  
  int horDir(int mult){
    if(this.dirMult * mult == 1){
      return 2;
    }
    
    return 3;
  }
  
  boolean whichDirHold(Fighter Opp, int mult){
    
    return (Opp.dirMult == 1*mult && Opp.inputs[2]) ||(Opp.dirMult == -1*mult && Opp.inputs[3]);
  }
  
    void AI_Controll(Fighter Opp){
    if(CurAction == Standing || CurAction == Crouching){
    for(int i = 0; i < AI_InputsScore.length; i++){
      if(AI_InputsScore[i] < 1. && inputs[i] && (dist(x, y, Opp.x, Opp.y) < abstand-5 || curHP > pHP) ){
        AI_InputsScore[i] += 0.1;
      }
      else if(AI_InputsScore[i] < 1. && inputs[i] && Opp.curHP < pOppHP){
        AI_InputsScore[i] += 0.1;
      }
      else if(AI_InputsScore[i] > 0 && inputs[i]){
             AI_InputsScore[i] -= 0.1; 
           }
           
               if(AI_InputsScore[i] > 1.){
                 AI_InputsScore[i] = 0.4;
    }
    }
    
    }
     if( AI_InputsScore[horDir(1)] < 3 && curHP < pHP){
             AI_InputsScore[0] += 0.1;
             AI_InputsScore[horDir(1)] += 0.1;
             AI_InputsScore[4] += 0.1;
             AI_InputsScore[5] += 0.1;
             AI_InputsScore[6] += 0.1;
           }
    
   String data = "";
    for(int i = 0; i < AI_InputsScore.length; i++){
      if( random(0, 1) < AI_InputsScore[i] ){
        inputs[i] = true;
      }
      else { inputs[i] = false; }
      
      data += AI_InputsScore[i] + ":";
    }
    println(data);
    
         for(int i = 0; i < firstPressInp.length; i++){
      if(!inputs[i]){
        firstPressInp[i] = true;
      }
    }
    
    
      if( !compareBoolArrs(pInputs, inputs) ){
        poop(true, 0, 0, 3);
        poop(true, 1, 0, 2);
        poop(true, 2, 1, 3);
        poop(true, 3, 1, 2);
      }
    
     for(int i = 0; i < pInputs.length; i++){
      pInputs[i] = inputs[i];
    }
    
    
    abstand = dist(x, y, Opp.x, Opp.y);
    pHP = curHP;
    pOppHP = Opp.curHP;
  }
  
  Fighter(){
  }
  
  Fighter(int x, int y, char[] charinputs){
    this.x = x;
    this.y = y;
    this.charinputs = charinputs;
  }
  
  Fighter(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    this(x, y, charinputs);
    this.tint = tint;
    this.AI_Controlled = AI_Controlled;
  }
  
    Fighter(int x, int y,  char[] charinputs, ControlDevice device){
    this.x = x;
    this.y = y;  
    this.charinputs = charinputs;
    if(device != null){
    this.device = device;
    setConDevice();
    }
  }
  
  void setConDevice(){
    DPad = device.getHat("DPAD");
    LightBut = device.getButton("LIGHT");
    MediumBut = device.getButton("MEDIUM"); 
    HeavyBut = device.getButton("HEAVY");
    RCBut = device.getButton("RC");
    LStick_X = device.getSlider("LSTICK-X"); 
    LStick_Y = device.getSlider("LSTICK-Y");
  }
  
  
  void setup(){
    
        int[] ani10 = {2};
  InHitStun = new Action(ani10, 0, 0, 0, 0, true, true, false, false);
  InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160));
  
  int[] ani11 = {11, 11};
  Knockdown = new Action(ani11, 0, 0, 0, 0, true, true, false, false);
  //Knockdown.gravMult = 0.92;
  Knockdown.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.updFrameDataArr(0, 1); 
  Knockdown.updFrameDataArr_float(0, 0, 0);
  Knockdown.addAllLists(1, 40, 0, 0);
   
   int[] ani13 = {2,2};
  softKD = new Action(ani13,
  0, 0, 0, 0, true, true, false, false);
  softKD.gravMult = 0.92;
  softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  softKD.updFrameDataArr(0, 1); 
  softKD.updFrameDataArr_float(0, 0, 0);
  softKD.addAllLists(1, 40, 0, 0);
  
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 200, 160, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
  specSetup();
  
  }
  
  void specSetup(){
  }
  
  void standingStateReturn(Fighter Opp){
    if(y == GROUNDHEIGHT || CollisionBox.bottom){
      curAirActions = maxAirActions;
    }
    
              if((y == GROUNDHEIGHT || CollisionBox.bottom) && inputs[1]){
        CurAction.playAction2(this, Opp, Crouching);
      }
      else if((y >= GROUNDHEIGHT || CollisionBox.bottom) && CurAction.attKind == 4){
        changeAction(Crouching);
      }
      else{
        CurAction.playAction2(this, Opp, Standing);
      }
  }
  
  private void gameLogic(Fighter Opp){
      deviceInput();
    
    if(AI_Controlled && millis() % 10 == 0){
      
      AI_Controll(Opp);
      AIControll2(Opp);
    }
    //chargeinputs
    for(int i = 0; i < inputs.length; i++){
      if(inputs[i] && inputChargeT[i] <= 1000){ 
        inputChargeT[i]++;
      }
      else if(inputChargeT[i] > 10){
        inputChargeT[i] -= inputChargeT[i]/10;
      }
      else{inputChargeT[i] = 0;}
      
    }

   //chargeinputs
   
   for(int i = 0; i < firstPressInp.length; i++){
      if(!inputs[i]){
        firstPressInp[i] = true;
      }
    }
    
     if( !compareBoolArrs(pInputs, inputs) ){
        poop(true, 0, 0, 3);
        poop(true, 1, 0, 2);
        poop(true, 2, 1, 3);
        poop(true, 3, 1, 2);
      }
        for(int i = 0; i < pInputs.length; i++){
      pInputs[i] = inputs[i];
    }
    
    standingStateReturn(Opp);
    
    
    fillInputBuffer();
    controll();
   
    updateColList();
    updateProjectiles(Opp);
    
    if(CurAnimation != null){
      CurAnimation.handleAnim();
        }
    
    
        curHP = constrain(curHP, 0, maxHP);
    curSuper = constrain(curSuper, 0, maxSuper);
    x = constrain(x, Camerabox.x - Camerabox.br/2, Camerabox.x + Camerabox.br/2);
    
    if(cancelWindow > 0){
      cancelWindow--;
    }
    
   extraStuff();
  }
 PImage[] last3Spr = new PImage[4];
 float[][] l3S_XY = new float[last3Spr.length][2];
 
  private void draw(){
    
    //sprite();
        
    pushMatrix();
    translate(x, y);
    if(facing == LEFT){
      scale(-1, 1);
    }
    
    if(tint){
    tint(240, 153, 255);
    }
    
    imageMode(CENTER);
    if(CurAnimation != null){
      
          if(tint){
    tint(240, 153, 255);
    }
      image( CurAnimation.Sprites[CurAnimation.curCollumn],
      CurAnimation.X_coords, 
      0 - CurAnimation.Sprites[CurAnimation.curCollumn].height/2 + CurAnimation.Y_coords);   
      
      
    }
    else{
        image(Sprs[CurAction.sprsIds[CurAction.curCollumn]], 0, 0 - Sprs[CurAction.sprsIds[CurAction.curCollumn]].height/2);
    }
    tint(255);
    popMatrix();
 
    /*fill(0, 0, 200, 90);
    drawColList(HurtBoxes);
    fill(200, 0, 0, 90);
    drawColList(HitBoxes);*/

    for(int i = Projectiles.size()-1; i >= 0; i--){
      Projectiles.get(i).draw();
    }

  }
  
  void extraStuff(){
  }
  
  boolean compareBoolArrs(boolean[] boolArr1, boolean[] boolArr2){
    for(int i = 0 ; i < boolArr1.length; i++){
      if(boolArr1[i] != boolArr2[i]){
        return false;
      }
    }
    
    return true;
  }
  
  private void deviceInput(){
    if(device != null){
      inputs[0] = DPad.up(); 
      inputs[1] = DPad.down(); 
      inputs[2] = DPad.right(); 
      inputs[3] = DPad.left();
      if(LStick_Y.getValue() >= 0.6){
        inputs[1] = true;
        inputs[0] = false;
      }
      else if(LStick_Y.getValue() <= -0.6){
        inputs[0] = true;
        inputs[1] = false;
      }
            if(LStick_X.getValue() >= 0.6){
        inputs[2] = true;
        inputs[3] = false;
      }
      else if(LStick_X.getValue() <= -0.6){
        inputs[3] = true;
        inputs[2] = false;
      }
      LStick_X.getValue(); 
      LStick_Y.getValue();
      inputs[4] = LightBut.pressed(); 
      inputs[5] = HeavyBut.pressed(); 
      inputs[6] = MediumBut.pressed(); 
      inputs[7] = RCBut.pressed();
    }
    
    
  }
  
  void keyPressed(){
    inputs = inputsKey(true, charinputs, inputs);
  }
  
  //fast Dash issue, weird reseting of input bools
  void poop(boolean setTo, int index, int n1, int n2){
    if(setTo && (!inputs[n1] || !inputs[n2])){
      firstPressDia[index] = setTo;
      firstPressInp[n1] = true;
      firstPressInp[n2] = true;
    }
    else if(!setTo && inputs[n1] && inputs[n2]){
      firstPressDia[index] = setTo;
      firstPressInp[n1] = false;
      firstPressInp[n2] = false;
    }
  }
  
  void keyReleased(){
    
    if(!AI_Controlled){
      inputs = inputsKey(false, charinputs, inputs);
    
    }
    
  }
  
void fighterActionsExtra(){}

void fighterActions(){
      if((CurAction == Standing || CurAction == bWalk|| CurAction == fWalk || (CurAction.hitCancel && cancelWindow > 0 && !inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){   
           normalWalk();
           jump();
           dash();    
           st_Normals();
    
       }
       else if( (CurAction == Crouching || (CurAction.hitCancel && cancelWindow > 0 && inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){
         cr_Normals();
    
         
       }
       else if( (CurAction == Standing || (CurAction.hitCancel && cancelWindow > 0) ) && y < GROUNDHEIGHT && !CollisionBox.bottom){
         dash();
         jump();
         j_Normals();
         
       }
       
       fighterActionsExtra();
}

void st_Normals(){}; void cr_Normals(){}; void j_Normals(){};


void normalWalk(){
      if(inputs[2]){
      if(facing == RIGHT){
        CurAction.reset();
        CurAction = fWalk;
        curSuper++;
      }
      else{
        CurAction.reset();
        CurAction = bWalk;
      }
    }
    
    if(inputs[3]){
       if(facing == LEFT){
        CurAction.reset();
        CurAction = fWalk;
        curSuper++;
      }
      else{
        CurAction.reset();
        CurAction = bWalk;
      }
    }
}
void jump(){
  
  int[] fJump = {9};
  int[] bJump = {7};
  Action l_ResultAction = CurAction;
  if(y == GROUNDHEIGHT || CollisionBox.bottom){
    if(compareBufferWithCombAtt(fJump)){
      l_ResultAction = fDiaJump;
    }
    else
    if(compareBufferWithCombAtt(bJump)){
      l_ResultAction = bDiaJump;
    }
    else
      if(inputs[0] && firstPressInp[0]
      ){
      l_ResultAction = Jumping;
    }
    
  }
  else
  if(curAirActions > 0){
        if(compareBufferWithCombAtt(fJump)){
      l_ResultAction = fDiaJump2;
    }
    else
    if(compareBufferWithCombAtt(bJump)){
      l_ResultAction = bDiaJump2;
    }
    else
      if(inputs[0] && firstPressInp[0]){
      l_ResultAction = Jumping2;
    }
  }
  
  if(l_ResultAction == Jumping || l_ResultAction == fDiaJump || l_ResultAction == bDiaJump || l_ResultAction == Jumping2 || l_ResultAction == fDiaJump2 || l_ResultAction == bDiaJump2){
           VisEffectsList.add(new VisualEffect(CollisionBox.x + CollisionBox.br/2, GROUNDHEIGHT - 125, dustJumpEff, 0));
       Soundeffects[2].cue(0);
       Soundeffects[2].play();
       if(inputbufferDir.size()-1 > 0){
       inputbufferDir.set(inputbufferDir.size()-1, 5);
       }
       curAirActions--;
       changeAction(l_ResultAction);
    }
  
}
void dash(){
  if(curAirActions > 0){
          if(compareBufferWithCombAtt(fDashmotion)){
            CurAction.reset();
          CurAction = FDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
    if(compareBufferWithCombAtt(bDashmotion)){
            CurAction.reset();
          CurAction = BDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
  }
}
  
  void controll(){ 
  
        if(HitStunT == 0 && CurAction != Blocking && CurAction != BeingGrapped && CurAction != InHitStun){
    
       
    if(inputs[1] && (y == GROUNDHEIGHT || CollisionBox.bottom) && !inputs[0] && CurAction == Standing){
      crouching = true;
      CurAction.reset();
      CurAction = Crouching;
    }
    else{
      crouching = false;
    }
    
       fighterActions();
       
    if( CurAction != Standing && inputs[7] && firstPressInp[7] && curSuper >= 50){
      changeAction(Standing);
      curSuper -= 50; slowMoDur = 40; slowMoValue = 4;
      println("RomanCancel");
      Soundeffects[1].cue(0);
      Soundeffects[1].play();
      VisEffectsList.add(new VisualEffect(x, y, RCEff, 0));
    }
        
        }    
        else if(HitStunT > 0){
      HitStunT--;
    }
    
        if(CurAction == Knockdown && Knockdown.curCollumn == 1){
      for(int i = 4; i < inputs.length; i++){
              if(inputs[i] && firstPressInp[i]){
                CurAction.reset();
                CurAction = Standing;
                      Soundeffects[1].cue(0);
      Soundeffects[1].play();
      VisEffectsList.add(new VisualEffect(x, y, RCEff, 0));
                break;
              }
    }
    }
    
            if(CurAction == softKD && softKD.curCollumn == 1 && y < GROUNDHEIGHT){
      for(int i = 4; i < inputs.length; i++){
              if(inputs[i] && firstPressInp[i]){
                CurAction.reset();
                CurAction = Standing;
                      Soundeffects[1].cue(0);
      Soundeffects[1].play();
      VisEffectsList.add(new VisualEffect(x, y, RCEff, 0));
                break;
              }
    }
    }
    else if(CurAction == softKD && softKD.curCollumn == 1 && y >= GROUNDHEIGHT){
                CurAction.reset();
                CurAction = Standing;
    }
    
  
    if(-0.1 > Force.x || Force.x > 0.1){
      x += Force.x;
    }
    if(!CollisionBox.bottom){
      y += Force.y;
    }
    
    if(CurAction.gravityActive && !CollisionBox.bottom){
      Force.y += gforce;
    }
    else{gforce = 0;}
    
    if( y < GROUNDHEIGHT && CurAction.gravityActive && !CollisionBox.bottom && gforce <= 3
    ){
      gforce += 0.2 * m + 0.01 * comboCount;
      gforce *= CurAction.gravMult;
      //println("grav:" + gforce);
    }
    else if(y > GROUNDHEIGHT){
      y = GROUNDHEIGHT;
      Force.y = 0;
      gforce = 0;
    }
        if(Force.x > 0){
      Force.x = Force.x - 0.1;
    }else
    if(Force.x < 0){
      Force.x = Force.x + 0.1;
    }
    
     
    
    for(int i = 0; i < firstPressInp.length; i++){
      if(inputs[i]){
        firstPressInp[i] = false;
      }
    }
    
    poop(false, 0, 0, 3);
    poop(false, 1, 0, 2);
    poop(false, 2, 1, 3);
    poop(false, 3, 1, 2);

  }
  
    void fillInputBuffer(){
    /*
    789
    456  10/Roof ; 11/Mid ; 12/Base
    123
    */
    int inputId = 5;
    if(inputs[0] && inputs[2] && firstPressDia[1]
    ){
      inputId = 9;
    }
    else if(inputs[0] && inputs[3] && firstPressDia[0]){
      inputId = 7;
    }
    else if(inputs[1] && inputs[2] && firstPressDia[3]){
      inputId = 3;
    }
    else if(inputs[1] && inputs[3] && firstPressDia[2]){
      inputId = 1;
    }
    else{
      if(inputs[0] && firstPressInp[0]){
        inputId = 8;
      }
      else if(inputs[1] && firstPressInp[1]){
        inputId = 2;
      }
      else if(inputs[2] && firstPressInp[2]){
        inputId = 6;
      }
      else if(inputs[3] && firstPressInp[3]){
        inputId = 4;
      }
      
    }
    
    IntList l_ButtonInputs = new IntList();

   if(inputs[4] && firstPressInp[4]){
     l_ButtonInputs.append(10);
   }
   if(inputs[5] && firstPressInp[5]){
     l_ButtonInputs.append(11);
   }
    
    if(inputId != 5){
      curInputTimer = 0;
       if(facing == LEFT){
          int l_Dir = inputId;
          if(l_Dir == 1 || l_Dir == 4 || l_Dir == 7){
            l_Dir += 2;
          }
          else if(l_Dir == 3 || l_Dir == 6 || l_Dir == 9){
            l_Dir -= 2;
          }
          inputId = l_Dir;
        }
      inputbufferDir.append(inputId);
      inputbufferBut.add(l_ButtonInputs);
    }
    
    if(inputbufferDir.size() > 15){
      inputbufferDir.remove(0);
    }
   
       if(inputbufferBut.size() > 15){
      inputbufferBut.remove(0);
    }
    
    if(curInputTimer < 10){
    curInputTimer++;
    }
    else{
      inputbufferDir.clear();
      inputbufferBut.clear();
      curInputTimer = 0;
    }

  }
  
  void sprite(){
    fill(0, 200, 0);
    CollisionBox.x = x - CollisionBox.br/2;
    CollisionBox.y = y;
    CollisionBox.draw();
    fill(200, 200, 0, 90);
    if(facing == RIGHT){
      triangle(x, y, x, y+40/2, x+40, y);
    }
    else{
      triangle(x, y, x, y+40/2, x-40, y);
    }
    line(x, y, x + Force.x*2, y + Force.y*2);
  }
  
  void drawColList(ArrayList<ColCircle> ColList){
    for(ColCircle c : ColList){
      c.draw(dirMult);
    }
  }
  
  /*void updColListWithActList(int i){
    HitBoxes.clear();
     for(int j = 0; i < EditAction.HitBoxCollect.get(i).size(); j++){
       HitBoxes.add(new ColCircle(EditAction.HitBoxCollect.get(i).get(j)));
       HitBoxes.get(j).addx *= dirMult;
       HitBoxes.get(j).forcex *= dirMult;
     }
     HurtBoxes.clear();
     for(int j = 0; i < EditAction.HurtBoxCollect.get(i).size(); j++){
       HurtBoxes.add(new ColCircle(EditAction.HurtBoxCollect.get(i).get(j)));
       HurtBoxes.get(i).addx *= dirMult;
       HurtBoxes.get(i).forcex *= dirMult;
     }
  }*/
  
  void updateColList(){
    for(ColCircle c : HurtBoxes){
      c.setxy(x, y);
    }
    for(int i = HitBoxes.size() - 1; i >= 0; i--){
      HitBoxes.get(i).setxy(x, y);
      if(HitBoxes.get(i).exTimer != -1 && HitBoxes.get(i).exTimer >= 1){
        HitBoxes.get(i).exTimer--;
      }
      else if(HitBoxes.get(i).exTimer == 0){
        HitBoxes.remove(i);
      }
    }
    
  }
  
 void changeXdirOfBoxes(){
        for(ColCircle c : HurtBoxes){
          c.addx *= -1;
          c.forcex *= -1;
    }
    for(ColCircle c : HitBoxes){
      c.addx *= -1;
      c.forcex *= -1;
    }
  }
  
  void facingCheckAndChange(Fighter Opp){
    if(CurAction == Standing || CurAction == Crouching){
    if(facing == RIGHT && Opp.x < x ){
      dirMult = -1;
      facing = LEFT;
      println("left");
          float l = AI_InputsScore[2];
    AI_InputsScore[2] = AI_InputsScore[3];
    AI_InputsScore[3] = l;
      
    }
    else if(facing == LEFT && Opp.x > x ){
      dirMult = 1;
      facing = RIGHT;
      println("right");
                float l = AI_InputsScore[2];
    AI_InputsScore[2] = AI_InputsScore[3];
    AI_InputsScore[3] = l;
    }
    }

  }
  
  Action operationsOnHit(Fighter Opp, ColCircle[] l_Boxes){
    Action resultAction = Opp.CurAction;
    
    if(l_Boxes != null){
    ColCircle HitBox = l_Boxes[0], c = l_Boxes[1];
    
     if( checkBlock(Opp, CurAction.attKind)
        ){
          //Opp.CurAction.reset();
          resultAction = Opp.Blocking;
          Force.x += -HitBox.forcex*2*dirMult + (CurAction.damage * -dirMult);
          //Force.y = -HitBox.forcey;
          Opp.Force.x = HitBox.forcex*dirMult + (CurAction.damage/10 + comboCount) * dirMult;
          //Opp.Force.y = HitBox.forcey / 10;
          Opp.Blocking.whenToUpdBoxs[0] = CurAction.affBlockStunT;
          
          if(Opp.inputs[1]){
            resultAction.AttAnim = Opp.LBlock;
            resultAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            resultAction.AttAnim = Opp.HBlock;
            resultAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }
          
          if(CurAction.firstHit){
            Opp.curHP -= CurAction.damage/10;
            CurAction.specialEffectOnHit(this, Opp);
            VisEffectsList.add(new VisualEffect(Opp.x + c.addx + c.br/2 * Opp.dirMult, y + HitBox.addy, BlockEff, CurAction.damage/4));
                Soundeffects[4].cue(0);
                Soundeffects[4].play();
          }
          
          if(!CurAction.multiHit){
            CurAction.firstHit = false;
          }
          frameFreeze = CurAction.damage/10;
  }
  
    else
      if(CurAction != BeingGrapped){
        Force.x =  (CurAction.damage/2 + comboCount) * -dirMult;
        Opp.Force.x = HitBox.forcex*dirMult + (CurAction.damage/4 + comboCount) * dirMult;
        Opp.Force.y = HitBox.forcey;
        Opp.HitStunT = CurAction.affHitStunT / (comboCount/2+1);
        

        
        if(Opp.y+10 < GROUNDHEIGHT && Opp.CurAction != Opp.softKD && Opp.CurAction != Opp.Knockdown){
          resultAction = Opp.softKD;
          Opp.softKD.whenToUpdBoxs[0] = CurAction.affHitStunT;
        }
        else if(CurAction.knocksDown && Opp.CurAction != Opp.Knockdown){
          resultAction = Opp.Knockdown;
          Opp.Knockdown.whenToUpdBoxs[0] = 20;//CurAction.affHitStunT;
        }
        
        else if(Opp.CurAction != Opp.Knockdown && Opp.CurAction != Opp.softKD){
          resultAction = Opp.InHitStun;
          Opp.InHitStun.whenToUpdBoxs[0] = CurAction.affHitStunT;
                            if(Opp.inputs[1]){
            resultAction.AttAnim = Opp.LHit;
            resultAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            resultAction.AttAnim = Opp.HHit;
            resultAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }
        }
        

        
        if(CurAction.firstHit){
          Opp.curHP -= CurAction.damage / (comboCount/2+1);
          curSuper += CurAction.damage;
          Opp.curSuper += CurAction.damage/2;
          CurAction.specialEffectOnHit(this, Opp);
             comboCount++;
             cancelWindow = 6;
          VisEffectsList.add(new VisualEffect(Opp.x + c.addx + c.br/2 * Opp.dirMult, y + HitBox.addy, HitEff, CurAction.damage/4));
                Soundeffects[0].cue(0);
                Soundeffects[0].play();
        }
        
        if(!CurAction.multiHit){
          CurAction.firstHit = false;
        }
        frameFreeze = CurAction.damage/10;
      }
    }
    
    return resultAction;
  }
  
  ColCircle[] checkHit(Fighter Opp){
      if(Opp.CurAction != Opp.InHitStun && Opp.CurAction != Opp.BeingGrapped && Opp.CurAction != Opp.Knockdown && Opp.CurAction != Opp.softKD){
             comboCount = 0;
           }
     
    ColCircle[] l_Boxes = null;

    for(ColCircle HitBox : HitBoxes){
    for(ColCircle c : Opp.HurtBoxes){

      if(HitBox.compare(c, dirMult, Opp.dirMult)){
        l_Boxes = new ColCircle[2];
        l_Boxes[0] = HitBox;
        l_Boxes[1] = c;
          
          break;
        }
        
        
      }
    }
 
    //Projektile //TO DO: Blocken hinzufügen
        for(int indexProj = Projectiles.size()-1; indexProj >= 0; indexProj--){
          Projectile p = Projectiles.get(indexProj);
    for(ColCircle c : Opp.HurtBoxes){
      if(p.HitBox.compare(c, 1, 1)){  
                if( checkBlock(Opp, p.attKind)    
                ){
          Opp.CurAction.reset();
          Opp.CurAction = Opp.Blocking;
          Opp.Force.x += p.forcex / 4;
          Opp.Force.y += p.forcey / 4;
          Opp.Blocking.whenToUpdBoxs[0] = p.hitStun/2;
          Opp.curHP -= CurAction.damage/10;
           VisEffectsList.add(new VisualEffect(p.x, p.y, BlockEff, p.damage/4));
                Soundeffects[0].cue(0);
                Soundeffects[0].play();
                
                    if(Opp.inputs[1]){
            Opp.CurAction.AttAnim = Opp.LBlock;
            Opp.CurAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            Opp.CurAction.AttAnim = Opp.HBlock;
            Opp.CurAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }

         if(p.destroyedByHit){
                     if(p.destrEff != null){
                        VisEffectsList.add(new VisualEffect(p.x, p.y, p.destrEff, 1));  
                        }
          Projectiles.remove(indexProj);

        }
          break;
        }
        else{
                Opp.Force.x += p.HitBox.forcex;
        Opp.Force.y += p.HitBox.forcey;
        Opp.curHP -= p.damage;
        Opp.HitStunT = p.hitStun;
        p.specialOnHit(this, Opp);
        comboCount++;

        if(Opp.y+10 < GROUNDHEIGHT){
          Opp.CurAction = Opp.softKD;
          Opp.Knockdown.whenToUpdBoxs[0] = p.hitStun;
        }
        else{
          Opp.CurAction.reset();
          Opp.CurAction = Opp.InHitStun;
          if(Opp.inputs[1]){
            Opp.CurAction.AttAnim = Opp.LHit;
            Opp.CurAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            Opp.CurAction.AttAnim = Opp.HHit;
            Opp.CurAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }
        }
        
           VisEffectsList.add(new VisualEffect(p.x, p.y, HitEff, p.damage/4));
                Soundeffects[0].cue(0);
                Soundeffects[0].play();
        if(p.destroyedByHit){
                 if(p.destrEff != null){
                   VisEffectsList.add(new VisualEffect(p.x, p.y, p.destrEff, 1));  
                 }
          Projectiles.remove(indexProj);
        }
        break;
      }
      }
    }
        }
        
        return l_Boxes;
      
  }
  
  boolean checkBlock(Fighter Opp, int attKind){
    return ((Opp.facing == LEFT && Opp.inputs[2]) || (Opp.facing == RIGHT && Opp.inputs[3]))
        && (
        ( (Opp.CurAction == Opp.Standing || Opp.CurAction == Opp.bWalk)
        && (attKind == CurAction.HIGH || attKind == CurAction.AIR || attKind == CurAction.MID || attKind == CurAction.NOTHING))
        || 
        ( (Opp.CurAction == Opp.Crouching ) 
        && (attKind == CurAction.LOW || attKind == CurAction.MID || attKind == CurAction.NOTHING))
        )
        
        ||
        
        (Opp.CurAction == Opp.Blocking 
        && (
        (attKind == CurAction.MID || attKind == CurAction.NOTHING)
        ||
        (!Opp.inputs[1] && (attKind == CurAction.HIGH || attKind == CurAction.AIR) )
        || 
        (Opp.inputs[1] && attKind == CurAction.LOW )
        ) 
        );
  }
  
  void updateProjectiles(Fighter Opp){
   for(int i = Projectiles.size()-1; i >= 0; i--){
      Projectile p = Projectiles.get(i);
      p.gameLogic(this, Opp);
      
      if(Projectiles.get(i).exTimer == 0){
               if(p.destrEff != null){
                  VisEffectsList.add(new VisualEffect(p.x, p.y, p.destrEff, 1));  
               }
        Projectiles.remove(i);
      }
    }
  }
  
   boolean compareBufferWithCombAtt(int[] dirsOfAtt){
    IntList DirBuffer = inputbufferDir;
    
    if(DirBuffer.size() < dirsOfAtt.length){
      return false;
    }
    else{
      for(int i = 0; i < dirsOfAtt.length; i++){
     
 
        if(facing == RIGHT){
          
        if(DirBuffer.get(DirBuffer.size() - dirsOfAtt.length + i) == dirsOfAtt[i]){
         
        }
        else{
          return false;
        }
        
        
        }
        
        else if(facing == LEFT){
          int l_Dir = DirBuffer.get(DirBuffer.size() - dirsOfAtt.length + i);
          /*if(l_Dir == 1 || l_Dir == 4 || l_Dir == 7){
            l_Dir += 2;
          }
          else if(l_Dir == 3 || l_Dir == 6 || l_Dir == 9){
            l_Dir -= 2;
          }*/
          //DirBuffer.set(l_Dir, DirBuffer.size() - dirsOfAtt.length + i);
        if( l_Dir == dirsOfAtt[i]){
         
        }
        else{
          return false;
        }
        }
      
      }
      
    }
    
    return true;
  }
  
 
  void changeAction(Action a){
    CurAction.reset();
    CurAction = a;
    if(a.AttAnim != null){
      CurAnimation = a.AttAnim;
      CurAnimation.timer = 0; CurAnimation.curCollumn = 0;
    }
    else{ CurAnimation = null;}
  }
 
  
}//Ende Hauptklasse

//FAMILIENHAUS###############################################################################################################################################################################################
class F_FHaus extends Fighter{

  Action st_fHeavy;
  Action BaseNormal;
  Action RoofNormal;
  ProjAction Fireball;
  ProjAction Fireball2;
  Action Special1;
  Action Special3;
  Action Special4;
  Animation FB1AnimUp, FB1AnimMid, FB1AnimDown, BettDestrEff;
  
  F_FHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_FHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
    F_FHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
void specSetup(){
   CollisionBox = new ColRect(0, 0, 150, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
    int[] etimes0 = {10, 10, 10};
    int[] etimes1 = {3, 3, 5, 5, 3, 3, 3, 3};
  FB1AnimUp = new Animation(etimes0, 0, 0, 3, "Projectiles/Bett/FH-BettUp");
  FB1AnimMid = new Animation(etimes0, 0, 0, 3, "Projectiles/Bett/FH-BettMid");
  FB1AnimDown = new Animation(etimes0, 0, 0, 3, "Projectiles/Bett/FH-BettDown");
  BettDestrEff = new Animation(etimes1, 0, 0, 8, "Effekte/BettDestrEff/FH-BettEff");
  
  int[] times0 = {5, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
  
      
  int x1 = 0;
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, x1, 7, "FHouse/FH-fWalk/FH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 0, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2, 0);
  
  int x3 = 0;
  int[] times3 = {1, 2, 1, 4};
  Animation Anim3 = new Animation(times3, x3, 4, "FHouse/FH-n.jump/FH-n.jump");
  Anim3.loop = false;
  Jumping = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);
  Jumping.resetAnim = true;

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  Jumping.addAllLists(1, 2, 0, -20);
  
  fDiaJump = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);
  fDiaJump.resetAnim = true;

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
      fDiaJump.addAllLists(1, 2, 11, -20);
  
  bDiaJump = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);
  bDiaJump.resetAnim = true;

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bDiaJump.addAllLists(1, 2, -7.5, -20);
  
  Jumping2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  
  int[] ani4 = {1, 9};
  FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 10, 0);
    FDash.addAllLists(1, 5, 14, 0);
    
  int[] ani5 = {1, 10};
  BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, -8, 0);
  BDash.addAllLists(1, 5, -10, 0);

  
  
  int[] ani6 = {2, 3, 4, 5, 5};
  EditAction = new Action(ani6, 60, 8, 4, CurAction.LOW, true, true, true, false);
  EditAction.updFrameDataArr(0, 10);
  EditAction.updFrameDataArr_float(0, 0, 0);
  EditAction.addAllLists(1, 4, 2, -2);
  EditAction.addAllLists(2, 4, 2, -2);
  EditAction.HitBoxCollect.get(2).add(new ColCircle(120, -50, 200, 160, 3, -5, -1));
  EditAction.addAllLists(3, 15, 0, 0);
  
  int [] times7 = {4, 4, 4, 4, 3, 8, 11, 11};
  Animation Anim = new Animation(times7, 0, 8, "FHouse/FH-sp2-gr/FH-2sp-gr");
  Fireball = new FHaus_ProjAction(Anim, 20, 15, 12, 0, false, false, false, false, 1.99, 80, 5.4, 0, true, true, false, FB1AnimUp, FB1AnimMid, FB1AnimDown);
  Fireball.updFrameDataArr(0, 19);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball.addAllLists(1, 25, 0, 0);
  Fireball.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball.ProjAnim = FB1AnimMid;
  Fireball.destrEffAnim = BettDestrEff;
  
  
    //int[] ani7 = {2, 1, 1};
  Fireball2 = new FHaus_ProjAction(Anim, 20, 15, 12, 0, false, false, false, false, 1.8, 80, 5.4, -5, true, true, false, FB1AnimUp, FB1AnimMid, FB1AnimDown);
  Fireball2.updFrameDataArr(0, 19);
  Fireball2.updFrameDataArr_float(0, 0, 0);
  Fireball2.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball2.addAllLists(1, 25, 0, 0);
  Fireball2.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball2.ProjAnim = FB1AnimMid;
  Fireball2.destrEffAnim = BettDestrEff;
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action(new Animation(times2, 0, 4, "FHouse/FH-cr/HF-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-HHitSt"); LHit = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-LHitSt");
  HBlock = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-HBlockSt"); LBlock = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-LBlockSt");
  
  int[] ani9 = {6};
  Blocking = new Action(ani9);
  Blocking.updFrameDataArr(0, 2); 
  Blocking.updFrameDataArr_float(0, 0, 0);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 200, 160));
  
  //int[] ani10 = {2};
  //InHitStun = new Action(ani10, 0, 0, 0, 0, true, true, false, false);
  //InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160));
  
  //int[] ani11 = {11, 11};
  //Knockdown = new Action(ani11, 0, 0, 0, 0, true, true, false, false);

  
  int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD");
  //softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));

  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  
  int[] aniGrab = {6, 6, 7, 7};
  float[][] xyGrab = {{100, -80},
                      {120, -120},
                      {80, -100}, 
                    {140, -140}};
  GrabAction NormalGrab = new GrabAction(aniGrab, 0, 12, 4, -1, false, true, true, true, xyGrab); NormalGrab.hitCancel = false;
  NormalGrab.updFrameDataArr(0, 20); 
  NormalGrab.updFrameDataArr_float(0, 2, 0);
  NormalGrab.addAllLists(1, 20, 5, 0);
  NormalGrab.addAllLists(2, 20, 5, 0);
  NormalGrab.addAllLists(3, 2, 0, 0);
  
  int[] ani12 = {0, 7, 7};
  
      
  RoofNormal = new ChangeAction(ani12,  5, 2, 5, -1, true, true, false, false, NormalGrab); RoofNormal.hitCancel = false;
  RoofNormal.updFrameDataArr(0, 5); 
  RoofNormal.updFrameDataArr_float(0, 0, 0);
    RoofNormal.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  RoofNormal.addAllLists(1, 4, 3, 0);
  RoofNormal.HitBoxCollect.get(1).add(new ColCircle( 150, -125, 200, 160, 0, 0, -1));
  RoofNormal.HurtBoxCollect.get(1).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  RoofNormal.addAllLists(2, 3, 0, 0);
  RoofNormal.HurtBoxCollect.get(2).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  
  
    int x14 = 0;
    int[] times14 = {2, 2, 3, 2, 2};
  Animation Anim14 = new Animation(times14, x14, 5, "FHouse/FH-st.Light/FH-st.Light");
  LightNormal = new Action("FH-st-light", Anim14, 6, 6, 5, 3, true, false, false, false);

  
  int[] ani15 = {6, 7, 0, 0, 0};
  BaseNormal = new Action("MidNormal", ani15, 10, 5, 2, CurAction.HIGH, false, true, false, false);
  
    int x16 = 50;
    int[] times16 = {2, 2, 3, 2, 2, 5, 2, 1, 1, 2};
    Animation Anim16 = new Animation(times16, x16, 10,"FHouse/FH-st.Mid/FH-st.Mid");
  MidNormal2 = new Action("FH-st-med", Anim16,  12, 9, 15, 3, true, false, false, false);
  
    int x17 = 50;
    int[] times17 = {2, 2, 3, 2, 4, 6, 2, 1, 1, 2, 2, 2};
    Animation Anim17 = new Animation(times17, x17, 12,"FHouse/FH-st.Heavy/FH-st.Heavy");
  HeavyNormal = new Action("FH-st-heay", Anim17,  16, 12, 20, CurAction.MID, true, false, false, false);
  
  int[] times18 = {2, 2, 4, 2, 2};
  Animation Anim18 = new Animation(times18, 25, 5, "FHouse/FH-cr.Light/FH-cr.Light");
  cr_LightNormal = new Action("FH-cr-light", Anim18,  6, 5, 3, 3, true, false, false, false);

  
  int x19 = 50;
  int[] times19 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  Animation Anim19 = new Animation(times19, x19, 15, "FHouse/FH-cr.Mid/FH-cr.Mid");
  cr_MidNormal = new Action("FH-cr-med", Anim19,  60, 8, 4, CurAction.LOW, true, true, true, false);
 /*  cr_MidNormal.updFrameDataArr(0, 10);
  cr_MidNormal.updFrameDataArr_float(0, 0, 0);
  cr_MidNormal.addAllLists(1, 4, 2, -2);
  cr_MidNormal.addAllLists(2, 4, 2, -2);
  cr_MidNormal.HitBoxCollect.get(2).add(new ColCircle(120, -50, 200, 160, 3, -5, -1));
  cr_MidNormal.addAllLists(3, 15, 0, 0);*/
  
    int[] times23 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  cr_HeavyNormal = new Action( "FH-cr-heay", new Animation(times23, 50, 12, "FHouse/FH-cr.Heavy/HF-cr.Heavy"),
                               16, 8, 24, CurAction.MID, true, false, false, false);
  
    int x20 = 50;
  int[] times20 = {2, 2, 2, 2, 2};
  Animation Anim20 = new Animation(times20, x20, 50, 5, "FHouse/FH-j.Light/FH-j.Light");
  j_LightNormal = new Action("FH-j-light", Anim20, 5, 4, 3, CurAction.AIR, true, true, false, false);
  
  int[] times22 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  j_MidNormal = new extraForceOnHitAct( "FH-j-med", new Animation(times22, 0, 90, 10, "FHouse/FH-j.med/HF-j.med"),
                               12, 7, 14, CurAction.AIR, true, true, false, false, 3, -16);
  
  int[] times21 = {3, 4, 3, 4, 3, 2, 2};
  Animation Anim21 = new Animation( times21, 50, 50, 7, "FHouse/FH-j.Heavy/FH-j.Heavy");
  j_HeavyNormal = new Action("FH-j-heay", Anim21, 8, 5, 21, CurAction.AIR, true, true, false, false);
  j_HeavyNormal.updFrameDataArr_float(1, -6, -10);
  
  int[] times24 = {2, 2, 2, 2, 4, 2, 2, 2};
  Special1 = new Action("FH-sp1", new Animation( times24, 50, 8, "FHouse/FH-sp1/FH-1special"), 18, 16, 6, CurAction.MID, true, true, true, true);
   
   int[] times25 = {4, 3, 6, 2, 3, 6, 3, 3, 3};
   st_fHeavy = new Action("FH-st-forH",//"data/FH-st-fHeavy", 
   new Animation( times25, 75, 9, "FHouse/FH-st.fH/FH-st-fH"), 18, 14, 6, CurAction.HIGH, true, false, false, false);
   
    int[] times21p3 = {4, 4, 4, 4, 4};
    Action Special3p3 = new Action( //"HH-cr-Light",
    new Animation(times21p3, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-3p"), 6, 5, 4, 0, true, true, false, false);
    
    Action FillAct = Special3p3;
    FillAct.updFrameDataArr(0, 20);
  FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p2 = {2, 2, 2};
    Action Special3p2 = new ChangeOnCondAct( "FH-sp3p2",
    new Animation(times21p2, 25, 50, 3,"FHouse/FH-sp3/FH-3sp-2p"), 20, 10, 20, CurAction.HIGH, true, false, true, false, Special3p3);
    
         FillAct = Special3p2;
    FillAct.updFrameDataArr(0, 60);
  FillAct.updFrameDataArr_float(0, 16, 18);
   //FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p1 = {2, 2, 2, 2, 2};
    Special3 = new ChangeOnEndAct( //"HH-cr-Light",
    new Animation(times21p1, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-1p"), 6, 5, 4, CurAction.AIR, false, false, false, false, Special3p2); 
          FillAct = Special3;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
     int[] times26 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  Special4 = new Action("FH-sp4", new Animation( times26, 0, 14, "FHouse/FH-sp4/FH-4sp"), 10, 6, 10, CurAction.HIGH, true, true, true, true);
            FillAct = Special4;
    //FillAct.updFrameDataArr(0, 28);
    FillAct.updFrameDataArr_float(1, 14, -20);
    FillAct.updFrameDataArr_float(2, 4, 0);
    FillAct.updFrameDataArr_float(4, 4, 0);
   //FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
}
  void st_Normals(){
        if(CurAction == fWalk){
      Force.x =  dirMult * sqrt(sq(sin(fWalk.AttAnim.timer* 0.1))) * 6;
    }
    
        if(inputs[6] && firstPressInp[6]){
      changeAction(MidNormal2);
    }
    if(inputs[5] && firstPressInp[5]){
      changeAction(HeavyNormal);
    }
    if(inputs[4] && firstPressInp[4]){
      changeAction(LightNormal);
    }
    if(inputs[5] && firstPressInp[5] && ( (inputs[2] && dirMult == 1) || (inputs[3] && dirMult == -1) )  ){
      changeAction(st_fHeavy);
    }   
    
                        int[] DPmotion2 = {6, 3, 2, 3, 6}; 
    if( (compareBufferWithCombAtt(DPmotion) || compareBufferWithCombAtt(DPmotion2)) && inputs[5] && firstPressInp[5]){
           changeAction( Special1);  //RoofNormal);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
                          if(compareBufferWithCombAtt(FBmotion) && inputs[4] && firstPressInp[4]){
           changeAction( Fireball2);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
                          if(compareBufferWithCombAtt(FBmotion) && inputs[6] && firstPressInp[6]){
           changeAction( RoofNormal);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
  }
  
  void cr_Normals(){
                    if(inputs[4] && firstPressInp[4]){
      changeAction(cr_LightNormal);
    } 
                if(inputs[6] && firstPressInp[6]){
      changeAction(cr_MidNormal);
    }     
         
               if(inputs[5] && firstPressInp[5]){
      changeAction(cr_HeavyNormal);
    }
  }
  
  void j_Normals(){
             if(inputs[4] && firstPressInp[4]){
           changeAction(j_LightNormal);
         }
                  if(inputs[6] && firstPressInp[6]){
           changeAction(j_MidNormal);
         }
                  if(inputs[5] && firstPressInp[5]){
           changeAction(j_HeavyNormal);
         }
         
                  if(compareBufferWithCombAtt(FBmotion) && inputs[5] && firstPressInp[5]){
          //CurAction.reset();
          //CurAction = BaseNormal;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
    }   
         
         int[] revFBm = {2, 1, 4};
                  if(compareBufferWithCombAtt(revFBm) && inputs[4] && firstPressInp[4]){         
          changeAction(Special3);
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }  
         
                  if(compareBufferWithCombAtt(FBmotion) && inputs[4] && firstPressInp[4]){
          changeAction( Fireball);
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }
  }
  
  void fighterActionsExtra(){
                            int[] DPmotion2 = {6, 3, 2, 3, 6}; 
    if( (compareBufferWithCombAtt(DPmotion) || compareBufferWithCombAtt(DPmotion2)) && inputs[5] && firstPressInp[5]){
           changeAction( Special1);  //RoofNormal);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         int[] revFB = {2, 1, 4}; 
    if( compareBufferWithCombAtt(revFB)  && inputs[5] && firstPressInp[5]){
           changeAction( Special4);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
  }
 
  
}

//HOCHHAUS###############################################################################################################################################################################################

class F_HHaus extends Fighter{
  Action RoofNormal;
  Action MidNormal;
  Action BaseNormal;
  Action Special1, Special2;
  ProjAction Fireball;
  Animation FB1Anim, FB1DestrEff;
  ProjAction Fireball2;
  ProjAction Fireball3;
  
  boolean hatOn = true;
  
  F_HHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {10, 10, 10, 10, 10, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 8, "HHouse/HH-st.idle/HH-st.idle");
      Standing = new Action("HH-st-idle", Anim0, false);
  }
  
  F_HHaus(int x, int y, char[] charinputs, boolean AI_Controlled){
    super(x, y, charinputs);
    this.AI_Controlled = AI_Controlled;
  }
  
   F_HHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
    F_HHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  

void specSetup(){
  m = 0.75;
  maxAirActions = 0;
  curAirActions = maxAirActions;
  CollisionBox = new ColRect(0, 0, 130, 200);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "HHouse/HHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  int[] etimes0 = {10, 10, 10, 10, 10};
  FB1Anim = new Animation(etimes0, 0, 0, 5, "Projectiles/Plane/HH-planeProjIdle");
  
  int[] etimes1 = {2, 2, 2, 2, 2, 2};
  FB1DestrEff = new Animation(etimes1, 0, 0, 6, "Effekte/PlaneDestrEff/HH-planeDestrEff");
  
  int[] times0 = {10, 10, 10, 10, 10, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 8, "HHouse/HH-st.idle/HH-st.idle");
      Standing = new Action("HH-st-idle", Anim0, false);
      
  int[] times2 = {5, 5, 5, 5, 5, 5, 5};
  Animation Anim1 = new Animation(times2, 30, 7,"HHouse/HH-fWalk/HF-fWalk");
      
  fWalk = new Action(//"HH-HeavyNormal",
  Anim1, 0, 0, 0, 0, true, false, false, false);
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 2.4, 0);
  fWalk.resetAnim = false;
  
  bWalk = new Action(//"HH-HeavyNormal",
  Anim1, 0, 0, 0, 0, true, false, false, false);
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -1.6, 0);
  bWalk.resetAnim = false;
  
  int[] times4 = {3, 2, 1, 4};
  Animation Anim4 = new Animation(times4, 0, 30, 4,"HHouse/HH-n.jump/HH-n.jump");
  
  Jumping = new Action(//"HH-HeavyNormal",
  Anim4, 0, 0, 0, 0, true, false, false, false);

      Jumping.updFrameDataArr(0, 10);
  Jumping.updFrameDataArr_float(0, 0, -18);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  
    fDiaJump = new Action(Anim4, 0, 0, 0, 0, true, false, false, false);

      fDiaJump.updFrameDataArr(0, 5);
  fDiaJump.updFrameDataArr_float(0, 8, -18);
  fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  
    bDiaJump = new Action(Anim4, 0, 0, 0, 0, true, false, false, false);

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, -6, -18);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  
  int[] ani4 = {1, 12, 1, 1};
  FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 10, 0);
    FDash.addAllLists(1, 5, 7, 0);
    
  int[] ani5 = {1, 11, 1, 1};
  BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, -5, 0);
  BDash.addAllLists(1, 5, -4, 0);
  
  
  int[] ani6 = {0, 5, 7, 4, 8};
  EditAction = new Action(ani6, 50, 15, 15, CurAction.LOW, true, true, true, false);
  EditAction.updFrameDataArr(0, 10);
  EditAction.updFrameDataArr_float(0, 0, 0);
  EditAction.addAllLists(1, 4, 2, 0);
  EditAction.addAllLists(2, 4, 2, 0);
  EditAction.HitBoxCollect.get(2).add(new ColCircle(120, -50, 300, 250, 3, -5, -1));
  EditAction.addAllLists(3, 15, 0, 0);
  
  int[] times7 = {3, 3, 3, 3, 4, 4, 4, 3, 2, 3, 3};
  Fireball = new HHaus_ProjAction1( new Animation(times7, 0, 11, "HHouse/HH-sp2/HH-2sp"),
  20, 15, 2, 0, false, false, false, false, 1, 300, -6.0, 0, false, false, true);
  Fireball.ProjAnim = FB1Anim;
  Fireball.destrEffAnim = FB1DestrEff;
  Fireball.updFrameDataArr(0, 15);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  Fireball.addAllLists(1, 20, 0, 0);
  Fireball.HurtBoxCollect.get(1).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  
  int[] aniFB2 = {11, 7, 7};
    Fireball2 = new HHaus_ProjAction2(aniFB2, 20, 15, 2, 0, false, false, true, false, 1, -1, 4.0, 0, false, false, false);
  Fireball2.updFrameDataArr(0, 25);
  Fireball2.updFrameDataArr_float(0, 0, 0);
  Fireball2.addAllLists(1, 40, 0, 0);
  
  int[] aniFB3 = {11, 3, 3};
    Fireball3 = new HHaus_ProjAction3(aniFB3, 0, 0, 0, 0, false, false, false, false, 1.2, 200, 2.0, 0, true, true, false);
  Fireball3.updFrameDataArr(0, 25);
  Fireball3.updFrameDataArr_float(0, 0, 0);
  Fireball3.addAllLists(1, 40, 0, 0);
  
    int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Crouching = new Action(new Animation(times1, 0, 7, "HHouse/HH-cr.idle/HH-cr.idl"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  
  //int[] times0_1 = {2,2};
  HHit = new Animation(times1, 0, 1, "HHouse/HH-Stun/HH-HHitSt"); LHit = new Animation(times1, 0, 1, "HHouse/HH-Stun/HH-LHitSt");
  HBlock = new Animation(times1, 0, 1, "HHouse/HH-Stun/HH-HBlockSt"); LBlock = new Animation(times1, 0, 1, "HHouse/HH-Stun/HH-LBlockSt");
  
  int[] ani9 = {3};
    Blocking = new Action(ani9);
  Blocking.updFrameDataArr(0, 2); 
  Blocking.updFrameDataArr_float(0, 0, 0);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 160, 300));
  
  //int[] ani10 = {11};
  //InHitStun = new Action(ani10);
  //InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 160, 300));
  
  //int[] ani11 = {10, 10};
  //Knockdown = new Action(ani11,  0, 0, 0, 0, true, true, false, false);
  
    int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"HHouse/HH-sKD/HH-air-sKD");
  
    int[] ani13 = {11};
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 160, 300, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
  int[] ani12 = {0, 9, 9};
  RoofNormal = new Action(ani12,  5, 2, 30, CurAction.HIGH, true, true, false, false);
  RoofNormal.updFrameDataArr(0, 15); 
  RoofNormal.updFrameDataArr_float(0, 0, 0);
  RoofNormal.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  RoofNormal.addAllLists(1, 4, 3, 0);
  RoofNormal.HitBoxCollect.get(1).add(new ColCircle( 150, -125, 160, 300, 0, 0, -1));
  RoofNormal.HurtBoxCollect.get(1).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  RoofNormal.addAllLists(2, 20, 0, 0);
  RoofNormal.HurtBoxCollect.get(2).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  
     int[] times15 = {2, 3, 1, 1, 2, 4};
  LightNormal = new Action("HH-st-light", new Animation(times15, 50, 6,"HHouse/HH-st.Light/HH-st.Light"),  5, 4, 2, 0, false, false, false, false);
  
   int[] times16 = {1, 1, 1, 1, 2, 2, 2, 3, 1, 1, 1, 1, 1, 1};
  MidNormal2 = new Action("HH-st-mid", new Animation(times16, 100, 14,"HHouse/HH-st.Mid/HH-st.Mid"),  12, 18, 30, 0, false, false, false, false);

  int[] times17 = {6, 6, 2, 2, 3, 2, 4, 7, 3, 3}; //35
  HeavyNormal = new Action("HH-HeavyNormal",
  new Animation(times17, 150, 10,"HHouse/HH-st.Heavy/HH-st.Heavy"), 20, 8, 28, 0, false, false, false, false);
  
    int[] times18 = {4, 5, 3, 2};
    cr_LightNormal = new Action( "HH-cr-Light",
    new Animation(times18, 25, 4,"HHouse/HH-cr.Light/HH-cr.Light"), 6, 5, 4, 0, false, false, false, false);
    
        int[] times19 = {3, 4, 4, 2, 4, 3, 2};
    cr_MidNormal = new Action( "HH-cr-med",
    new Animation(times19, 75, 7,"HHouse/HH-cr.med/HH-cr.med"), 20, 6, 12, CurAction.LOW, false, false, true, false);
    
        int[] times20 = {5, 5, 2, 2, 5, 3, 3};
    cr_HeavyNormal = new Action( "HH-cr-Heay",
    new Animation(times20, 25, 7,"HHouse/HH-cr.Heavy/HH-cr.Heavy"), 20, 15, 30, 0, false, false, false, false);
    
    
    int[] times21p3 = {2, 2, 2, 2};
    Action j_MidNormalp3 = new Action( //"HH-cr-Light",
    new Animation(times21p3, 30, 4,"HHouse/HH-j.med/p3/HF-j.med"), 6, 5, 4, 0, true, true, false, false);
    
    Action FillAct = j_MidNormalp3;
    FillAct.updFrameDataArr(0, 10);
  FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p2 = {2, 2};
    Action j_MidNormalp2 = new HoldButToKeepAct( "HH-j-medp2",
    new Animation(times21p2, 30, 2,"HHouse/HH-j.med/p2/HF-j.med"), 6, 5, 4, 0, false, false, false, true, j_MidNormalp3);
    
    int[] times21p1 = {2, 2, 2, 2, 2};
    j_MidNormal = new ChangeOnEndAct( //"HH-cr-Light",
    new Animation(times21p1, 30, 5,"HHouse/HH-j.med/p1/HF-j.med"), 6, 5, 4, 0, false, false, false, false, j_MidNormalp2); 
          FillAct = j_MidNormal;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
      int[] times22 = {3, 2, 4, 2};
    j_LightNormal = new Action( "HH-j-light",
    new Animation(times22, 30, 4,"HHouse/HH-j.light/FH-j.light"), 5, 3, 4, CurAction.AIR, true, true, false, false); 
    
          int[] times23 = {2, 2, 2, 2, 4, 2, 4, 2, 2, 2, 2};
    j_HeavyNormal = new Action( "HH-j-heay",
    new Animation(times23, 50, 30, 11,"HHouse/HH-j.Heavy/FH-j.Heavy"), 10, 7, 20, CurAction.HIGH, true, true, true, false); 
    j_HeavyNormal.gravMult = 0.93;
   
            int[] times24 = {3, 3, 2, 6, 2, 2, 4, 4, 2, 2};
    Special1 = new Action( "HH-sp1",
    new Animation(times24, 30, 10,"HHouse/HH-sp1/HH-1sp"), 15, 7, 12, CurAction.MID, true, true, false, true); 
    FillAct = Special1;
    FillAct.updFrameDataArr_float(1, 16, -1);
    //FillAct.updFrameDataArr_float(2, 12, -1);
    
      int[] times25 = {24, 5, 5, 5, 16, 8, 8};
  float[][] xyGrab = {{0, -280},
                      {-180, -120},
                      {-180, 0}, 
                    {0, 100}};
  GrabAction NormalGrab = new GrabAction(new Animation(times25, 0, 7,"HHouse/HH-sp3/HH-3sp-2p"), 30, 12, 11, -1, false, false, true, true, xyGrab);
  NormalGrab.updFrameDataArr(0, 20); 
  NormalGrab.updFrameDataArr_float(0, 0, 0);
  NormalGrab.addAllLists(1, 20, 0, -2);
  NormalGrab.addAllLists(2, 20, 0, -2);
  NormalGrab.addAllLists(3, 2, 0, -20);
  
      int[] times26 = {2, 2, 2, 3, 6, 6, 6, 3, 3};
  Special2 = new ChangeAction("HH-sp3-p1", new Animation(times26, 0, 9,"HHouse/HH-sp3/HH-3sp-1p"),
  5, 2, 5, -1, true, true, false, false, NormalGrab);
 /* Special2 .updFrameDataArr(0, 6); 
  
    Special2 .HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  Special2 .addAllLists(1, 12, 3, 0);
  
  Special2 .HurtBoxCollect.get(1).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  Special2 .addAllLists(2, 10, 0, 0);
  Special2 .HurtBoxCollect.get(2).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));*/
    Special2 .HitBoxCollect.get(1).add(new ColCircle( 10, -270, 280, 200, 0, 0, -1));
    Special2 .updFrameDataArr_float(0, 10, -20);
}

void dash(){
      if(compareBufferWithCombAtt(bDashmotion) && (y >= GROUNDHEIGHT || CollisionBox.bottom)
      ){
          CurAction.reset();
          CurAction = BDash;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }
}

boolean ChargeDirCheck(int chargeDur, int dir){
  
  return (inputChargeT[2] >= chargeDur && dirMult * dir == 1) || (inputChargeT[3] >= chargeDur && dirMult * dir == -1);
}

void st_Normals(){
      if(inputs[4] && firstPressInp[4]){
      changeAction(LightNormal);
    }
    if(inputs[5] && firstPressInp[5]){
      changeAction(HeavyNormal);
    }
    if(inputs[6] && firstPressInp[6]){
      changeAction(MidNormal2);
    }
    
               int[] backCharge = {6}; 
              if( ChargeDirCheck(20, -1) && inputs[4] && firstPressInp[4] && compareBufferWithCombAtt(backCharge) ){
          changeAction(Special1);

    }
     
    
             if(compareBufferWithCombAtt(FBmotion) && inputs[4] && firstPressInp[4]){
          CurAction.reset();
          CurAction = Fireball;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }   
    
        if(compareBufferWithCombAtt(FBmotion) && inputs[6] && firstPressInp[6]){
          CurAction.reset();
          CurAction = Fireball3;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }    
    
    int[] DPmotion2 = {6, 3, 2, 3, 6}; 
    if((compareBufferWithCombAtt(DPmotion) || compareBufferWithCombAtt(DPmotion2)) && inputs[5] && firstPressInp[5] && hatOn){
          CurAction.reset();
          CurAction = Fireball2;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
          hatOn = false;
    }
    int[] revFB = {2, 1, 4}; 
        if(compareBufferWithCombAtt(revFB) && inputs[4] && firstPressInp[4]){
          CurAction.reset();
          CurAction = Special2;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }
    
}

void cr_Normals(){
    if(inputs[4] && firstPressInp[4]){
      changeAction(cr_LightNormal);//EditAction;
    }
    if(inputs[5] && firstPressInp[5]){
      changeAction(cr_HeavyNormal);
    }
    if(inputs[6] && firstPressInp[6]){
      changeAction(cr_MidNormal);
    }
    
                   int[] backCharge = {6}; 
              if( ChargeDirCheck(20, -1) && inputs[4] && firstPressInp[4] && compareBufferWithCombAtt(backCharge) ){
          changeAction(Special1);
    }
    
}

void j_Normals(){
    if(inputs[6] && firstPressInp[6]){
      changeAction(j_MidNormal);
    }
    
    if(inputs[4] && firstPressInp[4]){
      changeAction(j_LightNormal);
    }
    
        if(inputs[5] && firstPressInp[5]){
      changeAction(j_HeavyNormal);
    }

}


void extraStuff(){
  for(int i = Projectiles.size()-1; i >= 0; i--){
    if(Projectiles.get(i) instanceof BoomerangProj){
        if(Projectiles.get(i).x >= CollisionBox.x - CollisionBox.br/2 &&  Projectiles.get(i).x <= CollisionBox.x + CollisionBox.br/2){
          Projectiles.remove(i);
          hatOn = true;
  }
 
    }   
     else{hatOn = true;}
  }
}



}


//Wohnwagen###############################################################################################################################################################################################
class F_WHaus extends Fighter{
  
  boolean stance = false;

  Action Stance, stanceFWalk, stanceBWalk;
  Action StanceLight, StanceMed, StanceHeavy;

  Action st_fHeavy;
  ProjAction Fireball;
  ProjAction Fireball2;
  Action Special1, Special2, Special3 ;
  
  F_WHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "WHouse/WH-st.idle/WH-st");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_WHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
    F_WHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
void setup(){
  maxAirActions = 1;
  curAirActions = maxAirActions;
   CollisionBox = new ColRect(0, 0, 120, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "WHouse/WH-st.idle/WH-st");
      Standing = new Action("FH-st-idle", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
  
      
  int x1 = 0;
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times0, x1, 1, "WHouse/WH-fWalk/WH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 3.2, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2.2, 0);
  
  Animation Anim1_1 = new Animation(times0, 0, 1, "WHouse/WH-stance/WH-stance");
      Stance = new Action( Anim1_1, false);
      Stance.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      Stance.updFrameDataArr(0, 1);
     Stance.updFrameDataArr_float(0, 0, 0);

  Animation Anim2_1 = new Animation(times0, x1, 1, "WHouse/WH-stance-fWalk/WH-stance-fW");
  stanceFWalk = new Action(Anim2_1, false);//0, 0, 0, 0, true, false, false, false);
  stanceFWalk.addingForce = true;
  stanceFWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  stanceFWalk.updFrameDataArr(0, 1);
  stanceFWalk.updFrameDataArr_float(0, 1.6, 0);//3, 0);
  
  Animation Anim2_2 = new Animation(times0, x1, 1, "WHouse/WH-stance-bWalk/WH-stance-bW");
  stanceBWalk = new Action(Anim2_2, false);
  stanceBWalk.addingForce = true;
  stanceBWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  stanceBWalk.updFrameDataArr(0, 1);
  stanceBWalk.updFrameDataArr_float(0, -1.2, 0);
  
  int[] times3 = {2, 3};
  Animation Anim3 = new Animation(times3, 0, 2, "WHouse/WH-nJump/WH-nJump");
  Anim3.loop = false;
  Jumping = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.addAllLists(1, 2, 0, -18);
  
  fDiaJump = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.addAllLists(1, 2, 10, -15);
  
  bDiaJump = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.addAllLists(1, 2, -7.5, -15);
  
    Jumping2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  int[] ani4 = {1, 9};
  FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 10, 0);
    FDash.addAllLists(1, 5, 10, 0);
    
  int[] ani5 = {1, 10};
  BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, -8, 0);
  BDash.addAllLists(1, 5, -8, 0);

  
  
  int[] ani6 = {2, 3, 4, 5, 5};
  EditAction = new Action(ani6, 60, 8, 4, CurAction.LOW, true, true, true, false);
  EditAction.updFrameDataArr(0, 10);
  EditAction.updFrameDataArr_float(0, 0, 0);
  EditAction.addAllLists(1, 4, 2, -2);
  EditAction.addAllLists(2, 4, 2, -2);
  EditAction.HitBoxCollect.get(2).add(new ColCircle(120, -50, 200, 160, 3, -5, -1));
  EditAction.addAllLists(3, 15, 0, 0);
  
  int[] ani7 = {2, 1, 1};
  Fireball = new ProjAction(ani7, 20, 15, 12, 0, false, false, false, false, 1.6, 200, 4.0, 0, true, true, false);
  Fireball.updFrameDataArr(0, 20);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.addAllLists(1, 25, 0, 0);
  
    //int[] ani7 = {2, 1, 1};
  Fireball2 = new ProjAction(ani7, 20, 15, 12, 0, false, false, false, false, 1.6, 200, 4.0, -4, true, true, false);
  Fireball2.updFrameDataArr(0, 25);
  Fireball2.updFrameDataArr_float(0, 0, 0);
  Fireball2.addAllLists(1, 30, 0, 0);
  
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action(new Animation(times2, 0, 1, "WHouse/WH-cr/WH-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-HHitSt"); LHit = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-LHitSt");
  HBlock = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-HBlockSt"); LBlock = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-LBlockSt");
  
  int[] ani9 = {6};
  Blocking = new Action(ani9);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 200, 160));
  
  int[] ani10 = {2};
  InHitStun = new Action(ani10, 0, 0, 0, 0, true, true, false, false);
  InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160));
  
  int[] ani11 = {11, 11};
  Knockdown = new Action(ani11, 0, 0, 0, 0, true, true, false, false);
  Knockdown.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.updFrameDataArr(0, 1); 
  Knockdown.updFrameDataArr_float(0, 0, 0);
  Knockdown.addAllLists(1, 40, 0, 0);
  
    int[] times0_0 = {2,2}; 
  softKD = new Action(new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD"),
  0, 0, 0, 0, true, true, false, false);
  softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  softKD.updFrameDataArr(0, 1); 
  softKD.updFrameDataArr_float(0, 0, 0);
  softKD.addAllLists(1, 40, 0, 0);
  
  int[] aniGrab = {6, 6, 7, 7};
  float[][] xyGrab = {{100, -80},
                      {120, -120},
                      {80, -100}, 
                    {140, -140}};
  GrabAction NormalGrab = new GrabAction(aniGrab, 0, 12, 0, 0, false, true, true, false, xyGrab);
  NormalGrab.updFrameDataArr(0, 20); 
  NormalGrab.updFrameDataArr_float(0, 2, 0);
  NormalGrab.addAllLists(1, 20, 5, 0);
  NormalGrab.addAllLists(2, 20, 5, 0);
  NormalGrab.addAllLists(3, 2, 0, 0);
     
  
  int[] ani13 = {11};
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 200, 160, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
    int[] times14 = {4, 2, 4, 4};
  Animation Anim14 = new Animation(times14, 0, 4, "WHouse/WH-st-light/WH-st-light");
  LightNormal = new Action("WH-st-light", Anim14, 8, 6, 5, 3, true, true, false, false);
  
    int[] times16 = {2, 4, 4, 3, 2, 5, 3, 2, 2, 2};
    Animation Anim16 = new Animation(times16, 70, 10,"WHouse/WH-st-med/WH-st-med");
  MidNormal2 = new Action("WH-st-med", Anim16,  16, 10, 15, 3, true, true, false, false);
  
    int[] times17 = {4, 4, 4, 2, 6, 6, 4, 2};
    Animation Anim17 = new Animation(times17, 70, 8,"WHouse/WH-st-heay/WH-st-heay");
  HeavyNormal = new Action("WH-st-heay", Anim17,  18, 12, 20, CurAction.MID, true, true, false, false);
  
  int[] times18 = {5, 6, 4};
  Animation Anim18 = new Animation(times18, 0, 3, "WHouse/WH-cr-light/WH-cr-light");
  cr_LightNormal = new Action("WH-cr-light", Anim18,  6, 5, 3, 3, true, false, false, false);

  int[] times19 = {4, 4, 2, 2, 4, 4, 2, 2, 2, 4, 4};
  Animation Anim19 = new Animation(times19, 70, 11, "WHouse/WH-cr-med/WH-cr-med");
  cr_MidNormal = new Action("Wh-cr-med", Anim19,  60, 8, 4, CurAction.LOW, true, true, true, false);
 /*  cr_MidNormal.updFrameDataArr(0, 10);
  cr_MidNormal.updFrameDataArr_float(0, 0, 0);
  cr_MidNormal.addAllLists(1, 4, 2, -2);
  cr_MidNormal.addAllLists(2, 4, 2, -2);
  cr_MidNormal.HitBoxCollect.get(2).add(new ColCircle(120, -50, 200, 160, 3, -5, -1));
  cr_MidNormal.addAllLists(3, 15, 0, 0);*/
  
    int[] times23 = {6, 3, 3, 6, 4, 4, 2, 6};
  cr_HeavyNormal = new Action( "Wh-cr-heay", new Animation(times23, 0, 8, "WHouse/WH-cr-heay/WH-cr-heay"),
                               16, 8, 40, CurAction.MID, true, true, true, false);
  
  int[] times20 = {3, 2, 4, 3};
  Animation Anim20 = new Animation(times20, 30, 0, 4, "WHouse/WH-j-light/WH-j-light");
  j_LightNormal = new Action("Wh-j-light", Anim20, 5, 4, 3, CurAction.AIR, true, true, false, false);
  
  int[] times22 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  j_MidNormal = new extraForceOnHitAct( "FH-j-med", new Animation(times22, 0, 90, 10, "FHouse/FH-j.med/HF-j.med"),
                               12, 7, 14, CurAction.AIR, true, true, false, false, 3, -16);
  
  int[] times21 = {3, 3, 3, 4, 3, 4, 2, 2, 2, 4, 4, 2};
  Animation Anim21 = new Animation( times21, 50, 0, 12, "WHouse/WH-j-heay/WH-j-heay");
  j_HeavyNormal = new Action("Wh-j-heay", Anim21, 8, 5, 6, CurAction.AIR, true, true, false, true);
  j_HeavyNormal.gravMult = 0.90;
           Action FillAct = j_HeavyNormal;
   FillAct.updFrameDataArr_float(0, 14, 0);
  
  int[] times24 = {2, 2, 2, 2, 4, 2, 2, 2};
  Special1 = new Action("FH-sp1", new Animation( times24, 50, 8, "FHouse/FH-sp1/FH-1special"), 18, 16, 8, CurAction.MID, true, true, true, true);
   
   int[] times25 = {4, 3, 6, 2, 3, 6, 3, 3, 3};
   st_fHeavy = new Action("FH-st-forH",//"data/FH-st-fHeavy", 
   new Animation( times25, 75, 9, "FHouse/FH-st.fH/FH-st-fH"), 18, 14, 20, CurAction.HIGH, true, false, false, false);
   
            int[] times27 = { 3, 6, 4, 2, 2, 3, 2, 2};
   Action Special2p2 = new Action(//"FH-st-forH",
      new Animation( times27, 0, 200, 8, "WHouse/WH-sp1/WH-1sp2p"), 18, 8, 18, CurAction.HIGH, true, false, false, false);
      Special2p2.hitCancel = false;
      FillAct = Special2p2;
   FillAct.updFrameDataArr(0, 15);
   FillAct.updFrameDataArr_float(0, 10, -20);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, 0, 330, 100));
      FillAct.HitBoxCollect.get(0).add(new ColCircle(30, 0, 360, 120));
   FillAct.addAllLists(1, 7, 0, 0);
      FillAct.HurtBoxCollect.get(1).add(new ColCircle(0, 0, 330, 100));
   FillAct.HitBoxCollect.get(1).add(new ColCircle(30, 0, 360, 120));
   FillAct.addAllLists(2, 2, 0, 0);
   
      int[] times26 = {3, 3, 3, 2, 2};
   Special2 = new ChangeOnEndAct(//"FH-st-forH",
   new Animation( times26, 0, 5, "WHouse/WH-sp1/WH-1sp"), 18, 6, 10, CurAction.HIGH, true, true, false, false, Special2p2);
   Special2.hitCancel = false;
   FillAct = Special2;
   FillAct.updFrameDataArr(0, 6);
   FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   FillAct.addAllLists(1, 4, 6, 0);
   FillAct.addAllLists(2, 1, 24, -42);
   
         int[] times28 = {3, 3, 2, 2, 2, 4, 2, 2, 2, 2};
   Special3 = new Action("Wh-sp2",
   new Animation( times28, 100, 10, "WHouse/WH-sp2/WH-2sp"), 32, 10, 4, CurAction.MID, true, true, false, false);
   Special3.hitCancel = false;
  
    int[] etimes1 = {2, 4, 3, 3};
  StanceLight = new Action("WH-stance-l", 
  new Animation(etimes1, 0, 4, "WHouse/WH-stance-light/WH-stance-l"), 6, 4, 5, CurAction.LOW, true, false, false, false);
  
    int[] etimes2 = {3, 3, 3};
  StanceMed = new Action("WH-stance-m", 
  new Animation(etimes2, 0, 3,"WHouse/WH-stance-Med/WH-stance-m"),  4, 1, 10, CurAction.MID, true, true, false, false);
  
    int[] etimes3 = {3, 3, 3, 3, 6};
  StanceHeavy = new Action("WH-stance-heay", 
  new Animation(etimes3, 0, 5,"WHouse/WH-stance-heavy/WH-stance-h"),  14, 12, 20, CurAction.HIGH, true, true, false, true);
}

  void standingStateReturn(Fighter Opp){
    if(stance){
      CollisionBox.ho = 100;
      CurAction.playAction2(this, Opp, Stance);
    }
    else if(!stance){
              if((y == GROUNDHEIGHT || CollisionBox.bottom) && inputs[1]){
        CurAction.playAction2(this, Opp, Crouching);
      }
      else if((y >= GROUNDHEIGHT || CollisionBox.bottom) && CurAction.attKind == 4){
        changeAction(Crouching);
      }
      else{
        CollisionBox.ho = 180;
        CurAction.playAction2(this, Opp, Standing);
      }
    }
    
      if(y >= GROUNDHEIGHT || CollisionBox.bottom){
        curAirActions = maxAirActions;
      }
          if(CurAction == InHitStun || CurAction == softKD || CurAction == Knockdown){
      stance = false;
    }

  }
  
  void fighterActionsExtra(){

    
    if( (stance && cancelWindow > 0) || CurAction == Stance || CurAction == stanceBWalk || CurAction == stanceFWalk){
            if(inputs[2]){
      if(facing == RIGHT){
        CurAction.reset();
        CurAction = stanceFWalk;
        curSuper++;
         }
      else{
        CurAction.reset();
        CurAction = stanceBWalk;
        curSuper++;
         }    
    }
    
        if(inputs[3]){
       if(facing == LEFT){
     
        CurAction.reset();
        CurAction = stanceFWalk;
        curSuper++;
         }
         else{
        CurAction.reset();
        CurAction = stanceBWalk;
        curSuper++;
         }
        }
        
    
        if(inputs[4] && firstPressInp[4]){
      changeAction(StanceLight);
    } 
        if(inputs[6] && firstPressInp[6]){
      changeAction(StanceMed);
      Force.x *= -1;
      dirMult *= -1;
      if(facing == RIGHT){
        facing = LEFT;
      }
      else if(facing == LEFT){
        facing = RIGHT;
      }
    } 
    if(inputs[5] && firstPressInp[5]){
      changeAction(StanceHeavy);
    } 
      
         int[] Stmotion = {2, 2}; 
    if( compareBufferWithCombAtt(Stmotion)){
      stance = false;
      //changeAction( Standing);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
                     if(inputs[5] && firstPressInp[5] && (Force.x >= 5 || Force.x <= -5) && ( (inputs[2] && dirMult == 1) || (inputs[3] && dirMult == -1) )){
      changeAction(Special2);
    }
       
    }
       
  }
  
 

  void st_Normals(){
    
        if(inputs[6] && firstPressInp[6]){
      changeAction(MidNormal2);
    }
                                     int[] HalfCircle = {4, 1, 2, 3, 6}; 
    if(// compareBufferWithCombAtt(HalfCircle) && 
    inputs[6] && firstPressInp[6]  && dirMult*Force.x <= -2){
           changeAction( Special3);  //RoofNormal);
         }
    
    if(inputs[5] && firstPressInp[5]){
      changeAction(HeavyNormal);
    }
        if(inputs[5] && firstPressInp[5] && (Force.x >= 5 || Force.x <= -5)){
          stance = true;
      changeAction(Special2);    
    }
    
    if(inputs[4] && firstPressInp[4]){
      changeAction(LightNormal);
    }
    
                        int[] DPmotion2 = {6, 3, 2, 3, 6}; 
    if( (compareBufferWithCombAtt(DPmotion) || compareBufferWithCombAtt(DPmotion2)) && inputs[5] && firstPressInp[5]){
           changeAction( Special1);  //RoofNormal);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
                          if(compareBufferWithCombAtt(FBmotion) && inputs[4] && firstPressInp[4]){
           changeAction( Fireball2);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
  }
  
  void cr_Normals(){
                    if(inputs[4] && firstPressInp[4]){
      changeAction(cr_LightNormal);
    } 
                if(inputs[6] && firstPressInp[6]){
      changeAction(cr_MidNormal);
    }     
         
               if(inputs[5] && firstPressInp[5]){
      changeAction(cr_HeavyNormal);
    }
  }
  
  void j_Normals(){
             if(inputs[4] && firstPressInp[4]){
           changeAction(j_LightNormal);
         }
                  if(inputs[6] && firstPressInp[6]){
           changeAction(j_MidNormal);
         }
                  if(inputs[5] && firstPressInp[5]){
           changeAction(j_HeavyNormal);
         }
           
         
                  if(compareBufferWithCombAtt(FBmotion) && inputs[4] && firstPressInp[4]){
          CurAction.reset();
          CurAction = Fireball;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }
  }
 
  
}

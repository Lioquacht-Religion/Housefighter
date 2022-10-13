abstract class Fighter{

  String name = "poopdipoop";
  final boolean RIGHT = false;
  final boolean LEFT = true;
  
  void printPDF(){
    float scale = 0.5;
    PGraphics pdf = createGraphics(2000, 9000, PDF, name + ".pdf");
    imageMode(CORNER);
    int nextHeight = 30;
    pdf.beginDraw();
    pdf.background(255);
    pdf.textSize(20);
    pdf.fill(200,0,0); 
    pdf.text(name + " - Datasheet", 20, 10);
    
    for(int i = 0; i < ActionList.size(); i++){
      for(int j = 0; j < ActionList.get(i).size(); j++){
        Action a = ActionList.get(i).get(j);
        
        if(a.AttAnim != null){
          int st = 0, at = 0, et = 0;
          for(int k = 0; k < a.HitBoxCollect.size(); k++){
            if(a.HitBoxCollect.get(k).size() == 0 && at == 0) st += a.whenToUpdBoxs[k];
            else if(a.HitBoxCollect.get(k).size() > 0) at += a.whenToUpdBoxs[k];
            else if(a.HitBoxCollect.get(k).size() == 0 && at > 0) et += a.whenToUpdBoxs[k];
          }
          String ActData = a.datnam + ": Damage: " + a.damage + "; AttWeight: " + a.attWeight + "; Hitstun: " + a.affHitStunT + "; Blockstun: " + a.affBlockStunT + "; Startup: " + st + "; Active: " + at + "; Endlag: " + et;
          

          Animation Anim = a.AttAnim;
          int heightExtra = 0;
          
          for(int k = 0, k2 = 0; k < Anim.Sprites.length; k++){
            if( (Anim.Sprites[k].width*scale*k)/(heightExtra+1) > pdf.width - (Anim.Sprites[k].width*scale) ){
              heightExtra++; k2 = k;
            }

            pdf.image(Anim.Sprites[k], Anim.Sprites[k].width*scale*(k-k2), nextHeight + Anim.Sprites[0].height*scale * heightExtra, Anim.Sprites[k].width*scale, Anim.Sprites[k].height*scale);
                pdf.textAlign(CENTER, CENTER); 
                pdf.textSize(40); pdf.fill(200, 0, 0);
            pdf.text(Anim.changeTimes[k], Anim.Sprites[k].width*scale*(k-k2)+30, nextHeight + Anim.Sprites[0].height*scale * heightExtra);
                pdf.textSize(34); pdf.fill(150);
            pdf.text(Anim.changeTimes[k], Anim.Sprites[k].width*scale*(k-k2)+30, nextHeight + Anim.Sprites[0].height*scale * heightExtra);
          }

          nextHeight += Anim.Sprites[0].height * scale * (heightExtra+1) + 80;
          heightExtra = 0;
          pdf.textSize(20); pdf.textAlign(CORNER); pdf.text(ActData, 10, nextHeight-60);
        }
                
      }
    }
    
    pdf.dispose();
    pdf.endDraw();
  }
  
  //weiter ausarbeiten
  void reset(){
    armorCount = 0;
  }
  
  PImage[] Sprs;
  
  PlControl PlContr = new PlControl();
  ControlDevice device = null; ControlButton LightBut, MediumBut, HeavyBut, RCBut, EMBut; ControlHat DPad; ControlSlider LStick_X, LStick_Y; boolean[] pInputs = new boolean[4];
  boolean[] inputs = new boolean[9]; //up , down, right, left, Roof, Mid, Base, utility(RC), Special;
  boolean[] firstPressInp = new boolean[inputs.length];
  boolean[] firstPressDia = {true, true, true, true};
  int[] inputChargeT = new int[inputs.length];
  char[] charinputs;
  InputRecord Recorder = new InputRecord();
  
  boolean AI_Controlled = false;
  boolean tint = false;
  boolean easyMode = false;
  
  IntList inputbufferDir = new IntList();
  ArrayList<IntList> inputbufferBut = new ArrayList<IntList>();
  int curInputTimer = 0;
  
  int comboCount = 0;
  
  int[] FBmotion = {2, 3, 6}, DPmotion = {6, 2, 3}, fDashmotion = {6, 6}, bDashmotion = {4, 4};
  
  float x, y;
  PVector Force = new PVector(0, 0);
  float gforce = 0;
  int dirMult = 1;
  float m = 1;
  
  ColRect CollisionBox;
  
  boolean facing = false;
  boolean grounded = false;
  boolean crouching = false;
  
  int maxHP = 200, curHP = maxHP;
  int maxSuper = 1000, curSuper = 0;
  int maxAirActions = 1, curAirActions = maxAirActions;
  
  int HitStunT = 0;  //wird das noch verwendet?
  int cancelWindow = 0, throwInvu = 0, armorCount = 0;
  boolean counterState = false, punishState = false;
  
  ArrayList<ColCircle> HurtBoxes = new ArrayList<ColCircle>();
  ArrayList<ColCircle> HitBoxes = new ArrayList<ColCircle>();
  
  ArrayList<Projectile> Projectiles = new ArrayList<Projectile>();
  
  float[] AI_InputsScore = {.1, .1, .2, .2, .8, .3, .3, .0, .3};
  int pHP = 200, pOppHP = 200; float abstand = 100;
  
  Animation CurAnimation = null;
  
  Action CurAction = new Action();
  
  Action Standing, Crouching, Falling, Landing, Turning;
  Action Knockdown, KDstandup, softKD, WallStick, WallBounce, GroundBounce, Juggle, Stagger;
  boolean[] hitstates = new boolean[5];
  Animation AnimKD, AnimKDreturn, AnimAirHit; 
  Action BeingGrapped;
  Action InHitStun;
  Animation HHit, LHit;
  Action Jumping, fDiaJump, bDiaJump, Jumping2, fDiaJump2, bDiaJump2;
  Action Blocking;
  Animation HBlock, LBlock;
  Animation wBflightB, wBstick, wBflightF,
    gBflightD, gBstick;
  
Action fWalk, bWalk, FDash, BDash, airFDash, airBDash;

Action EditAction, NormalThrow, AirThrow;

  Action LightNormal, MidNormal2, HeavyNormal, cr_LightNormal, cr_MidNormal2, cr_MidNormal, cr_HeavyNormal, j_LightNormal, j_MidNormal, j_HeavyNormal, j_DustNormal;

  ArrayList<ArrayList<Action>> ActionList = new ArrayList<ArrayList<Action>>();
  
  void checkSingleActList(Fighter Opp, ArrayList<Action> ActList){
    for(Action a : ActList){
      if(a != null){
        boolean l_check = false;
      for(int i = 0; i < a.Conds.length; i++){
          if(a.Conds[i].cond(this, Opp) && (CurAction.attWeight < a.attWeight || (a.selfCancel && CurAction == a) ) ){
            a.Conds[i].EffectIfCond(this, Opp);
            l_check = true;
          }else {l_check = false; break;}
      }
      if(l_check){
        changeAction(a); 
        if(cancelWindow > 0){ cancelWindow = 0; gforce = 0;}
      }
    }
    }
  }
  
  Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, FDash, BDash, Jumping, fDiaJump, bDiaJump},
  {cr_LightNormal, cr_MidNormal, cr_HeavyNormal},
  {j_LightNormal, j_MidNormal, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2},
  {}
};
  void fillActionsList(Action[][] ActTab){
    ActionList = new ArrayList<ArrayList<Action>>();
    for(int i = 0; i < ActTab.length; i++){
      ActionList.add(new ArrayList<Action>());
      for(int j = 0; j < ActTab[i].length
      ; j++){
        ActionList.get(i).add( ActTab[i][j]);       
      }
    }
    
  }
  

  
  Fighter(){
  }
  
  Fighter(int x, int y, char[] charinputs){
    this.x = x;
    this.y = y;
    //this.charinputs = charinputs;
    PlContr = new PlControl(charinputs);
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
    PlContr = new PlControl(device, charinputs);
  }
  
  Fighter(int x, int y, PlControl PlContr){
    this.x = x;
    this.y = y;  
    this.PlContr = PlContr;
  }
  
  Fighter(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten //WARNING: not in use anymore
    this( x, y, charinputs);
    m = F.m; maxHP = F.maxHP; curHP = maxHP;
    maxSuper = F.maxSuper; curSuper = 0;
    maxAirActions = F.maxAirActions; curAirActions = maxAirActions;
    ColRect cf = F.CollisionBox;
    CollisionBox = new ColRect(cf.x, cf.y, cf.br, cf.ho);
    Sprs = F.Sprs;
    for(int i = 0; i < F.ActionList.size(); i++){
      ActionList.add( new ArrayList<Action>() );
      
      for(int j = 0; j < F.ActionList.get(i).size(); j++){
        // copy references Animation( Spr-Array, times array, ), Cond Array, Hit and Hurtboxes, times array, force array
        //println(F.ActionList.get(i).get(j).datnam);
        ActionList.get(i).add(new Action( F.ActionList.get(i).get(j) ) );
      }
      
    }
    
    copyStdActions(F);
    
  }
  
  void copyStdActions(Fighter F){
        Standing = new Action(F.Standing); Crouching = new Action(F.Crouching);
    Knockdown = new Action(F.Knockdown); softKD = new Action(F.softKD); WallStick = new Action(F.WallStick); WallBounce = new Action(F.WallBounce);
    Juggle = new Action(F.Juggle); Stagger = new Action(F.Stagger); BeingGrapped = new Action(F.BeingGrapped);
    InHitStun = new Action(F.InHitStun); HHit = new Animation(F.HHit); LHit = new Animation(F.LHit);
    Blocking = new Action(F.Blocking); HBlock = new Animation(F.HBlock); LBlock = new Animation(F.LBlock);
    Jumping = new Action(F.Jumping); fDiaJump = new Action(F.fDiaJump); bDiaJump = new Action(F.bDiaJump);
    Jumping2 = new Action(F.Jumping2); fDiaJump2 = new Action(F.fDiaJump2); bDiaJump2 = new Action(F.bDiaJump2); // HHaus has no double jump -> reason for nullptrexcept
    fWalk = new Action(F.fWalk); bWalk = new Action(F.bWalk); FDash = new Action(F.FDash); BDash = new Action(F.BDash); // maybe adding costume constructor
  }
  
  void setConDevice(){
    DPad = device.getHat("DPAD");
    LightBut = device.getButton("LIGHT");
    MediumBut = device.getButton("MEDIUM"); 
    HeavyBut = device.getButton("HEAVY");
    RCBut = device.getButton("RC");
    EMBut = device.getButton("EM");
    LStick_X = device.getSlider("LSTICK-X"); 
    LStick_Y = device.getSlider("LSTICK-Y");
  }
  
  void stunStatesAnimSetup(){
     AnimKD = new Animation(new int[]{2, 2, 2, 2, 20} , 0, 5,"FHouse/FH-hKD/FH-Knockdown"); 
     AnimKD.loop = false;
     AnimKDreturn = new Animation(new int[]{2, 2, 2, 2} , 0, 4,"FHouse/FH-hKD/FH-KDreturn"); 
     AnimAirHit = new Animation(new int[]{4, 4, 4, 4, 4} , 0, 5,"FHouse/FH-sKD/FH-airHitstun");
     AnimAirHit.loop = false;
  }
  
  void setup(){
    stunStatesAnimSetup();
    fillActionsList(ActTab);
    
        int[] ani10 = {2};
  InHitStun = new Action(ani10, 0, 0, 0, 0, Action.HITSTATE, true, true, false, false);
  InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160));
  
  KDstandup = new Action(AnimKDreturn, 0, 0, 0, 0, Action.HITSTATE, true, false, false, false);
  KDstandup.updFrameDataArr(0, 16); 
  KDstandup.updFrameDataArr_float(0, 0, 0);

  
  //int[] ani11 = {11, 11};
  Knockdown = new Action(AnimAirHit, 0, 0, 0, 0, Action.HITSTATE, true, true, false, false);
  Knockdown.ActEffs = new ActEffect[]{
    new ChangeAnimTo(AnimAirHit, 1, new ActTimeCond2(0, 1) ),
    new ChangeAnimTo(AnimKD, 1, new ActTimeCond(0, 4, 0, 100), new Grounded(), new AnimCheck(AnimAirHit) 
    ),
    //new ChangeAnimTo(AnimKD, 1, new ActTimeCond(1, 1), new AnimCheck(AnimAirHit, true) ),
    //new ChangeAnimTo(AnimAirHit, 1, new ActTimeCond(1, 39) ) 
    new ChangeActTo(KDstandup, 1, new ActTimeCond(1, 40, 1, 80), new Grounded() )
  };
 //new ChangeAnimTo(AnimKD, 1, new ActTimeCond(1, 1), new AnimCheck(AnimAirHit), new Grounded() ),
  Knockdown.HurtBoxCollect.get(0).add(new ColCircle( 0, -40, 180, 80, 0, 0, -1));
  Knockdown.updFrameDataArr(0, 80); 
  Knockdown.updFrameDataArr_float(0, 0, -4);
  Knockdown.addAllLists(1, 80, 0, 0);

   int[] ani13 = {2,2};
  softKD = new Action(AnimAirHit,
  0, 0, 0, 0, Action.HITSTATE, true, true, false, false);
  softKD.ActEffs = new ActEffect[]{
    //new ChangeAnimTo(AnimKD, 1, new ActTimeCond(0, 0) ),
     new ChangeAnimTo(AnimAirHit, 1, new ActTimeCond2(0, 0) ), 
    new ChangeActTo(KDstandup, 1, new Grounded() )    
  };
  softKD.gravMult = 0.92;
  softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  softKD.updFrameDataArr(0, 1); 
  softKD.updFrameDataArr_float(0, 0, 0);
  softKD.addAllLists(1, 40, 0, 0);
  softKD.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  
  //Opp.WallStick, Opp.WallBounce, Opp.Juggle, Opp.GroundBounce, Opp.Stagger
  
  wBflightB = new Animation(new int[]{2} , 0, 1, "FHouse/FH-spHitStates/FH-wBounce/FH-wBounce1p");
  wBstick = new Animation(new int[]{4, 40} , 0, 2, "FHouse/FH-spHitStates/FH-wBounce/FH-wBounce2p");
  wBflightF = new Animation(new int[]{2} , 0, 1, "FHouse/FH-spHitStates/FH-wBounce/FH-wBounce3p");
  
  gBflightD = new Animation(new int[]{2} , 0, 1, "FHouse/FH-spHitStates/FH-gBounce/FH-gBounce1p");
  gBstick = new Animation(new int[]{4, 40} , 0, 2, "FHouse/FH-spHitStates/FH-gBounce/FH-gBounce2p");
  gBstick.loop = false;
  
    WallStick = new Action(wBstick, 0, 0, 0, 0,Action.HITSTATE, false, false, false, false);
  WallStick.gravMult = 0.92;
  WallStick.ActEffs = new ActEffect[]{new ChangeAnimTo(wBflightB, 1, new ActTimeCond2(0, 0)), 
  new ChangeAnimTo(wBstick, 1, new CamWallTouch()),  new SetForce(-20, 0, 1, new CamWallTouch() ), };
  WallStick.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  WallStick.updFrameDataArr(0, 58); WallStick.updFrameDataArr_float(0, -40, -10);
  //WallStick.addAllLists(1, 40, -5, 0);
  //WallStick.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  
  WallBounce = new Action(wBflightB, 0, 0, 0, 0,Action.HITSTATE, true, true, false, false);
  WallBounce.gravMult = 0.92; 
  WallBounce.ActEffs = new ActEffect[]{new ChangeAnimTo(wBflightB, 1, new ActTimeCond2(0, 0)), new ChangeAnimTo(wBstick, 1, new CamWallTouch()),
  new TimerEff( 10, new Condition[]{new CamWallTouch()}, new SetForce(34, -6, 1, new CamWallTouch() ), new ChangeAnimTo(wBflightF, 1) ) };  //new WallBounceEff(1, new Condition[]{new Condition()})};
  WallBounce.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  WallBounce.updFrameDataArr(0, 46); WallBounce.updFrameDataArr_float(0, -40, -10);
  
  GroundBounce = new Action(gBflightD, 0, 0, 0, 0,Action.HITSTATE, true, true, false, false);
  //WallBounce.gravMult = 0.92; 
  GroundBounce.ActEffs = new ActEffect[]{new ChangeAnimTo(gBflightD, 1, new ActTimeCond2(0, 0)), 
  new ChangeAnimTo(gBstick, 1, new Grounded()), new TimerEff(10, new Condition[]{new Grounded()}, new SetForce(0, -20, 1, new Grounded()))};
  GroundBounce.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  GroundBounce.updFrameDataArr(0, 40); GroundBounce.updFrameDataArr_float(0, 0, 20);
  
  Juggle = new Action(new Animation(new int[]{6, 6, 6, 6, 6} , 0, 5, "FHouse/FH-spHitStates/FH-juggle/FH-juggle"), 0, 0, 0, 0, Action.HITSTATE, false, false, false, false);
  Juggle.gravMult = 1; Juggle.ActEffs = new ActEffect[]{
  new ForceAddEff(0, 0, false), new ForceAddEff(0, 4, true), new GravEff(0, 0, false), new GravEff(0, 4, true), new ChangeActTo(softKD, 1, new ActTimeCond2(1, 24))}; 
  Juggle.updFrameDataArr(0, 5); Juggle.updFrameDataArr_float(0, -2, -20);
  Juggle.addAllLists(1, 48, -3, 0);
  Juggle.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1)); Juggle.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  
  Stagger = new Action(new Animation(new int[]{6, 6, 6}, 25, 3, "FHouse/FH-spHitStates/FH-stagger/FH-stagger2p"), 0, 0, 0, 0, Action.HITSTATE, false, false, false, false);
  Stagger.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Stagger.updFrameDataArr(0, 90); Stagger.updFrameDataArr_float(0, -5, 0);
  
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, Action.HITSTATE, false, true, false, false); BeingGrapped.collision = false;
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 200, 160, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
    float[][] xyGrab = {{600, -80},
                      {320, -320},
                      {300, -100}, 
                    {400, 40}};
  Action NormalGrab = new Action("FH-st-idle", 
  new Animation( new int[]{6, 4, 2, 4, 4, 4, 4, 4, 4, 6, 4, 2, 2} , 70, 13, "FHouse/FH-nthrow/p2/FH-nthrow"), 
  new Condition[]{new Condition()}, 
  2, 0, 0, Action.GRAB, 100, true, false, false, false); NormalGrab.hitCancel = false;
 NormalGrab.ActEffs = new ActEffect[]{new GrabEff(xyGrab , new int[]{20,10,5,20}), new PutInHitSt(30, 10, new ActTimeCond2(0, 18) )};
  NormalGrab.updFrameDataArr(0, 20); 
  NormalGrab.updFrameDataArr_float(0, 2, 0);
  NormalGrab.addAllLists(1, 20, 5, 0);
  NormalGrab.addAllLists(2, 20, 5, 0);
  NormalGrab.addAllLists(3, 2, 0, 0);

  Condition[] Cond00 ={new facingCond(1), new fPButCond(8)};//, new fPButCond(5)  };// ;
  NormalThrow = new Action("FH-nthrow", 
  new Animation( new int[]{2, 2, 6, 4, 2} , 70, 5, "FHouse/FH-nthrow/p1/FH-nthrow"), 
 Cond00, 
  10, 0, 0, Action.GRAB, 1, true, false, false, false);
 NormalThrow.ActEffs = new ActEffect[]{new ChangeActTo(NormalGrab, 0, new OppCheck(new Grounded()))};
 NormalThrow.hitCancel = false;
 
     float[][] xyGrab1 = {{200, -80},
                      {200, -80}};
  Action AirThrowP2 = new Action("FH-airthrowp2", 
  new Animation( new int[]{4, 4, 2, 6, 2, 6, 4, 4, 2}, 50, 40, 9, "FHouse/FH-airThrow/p2/FH-airThrow"), 
  new Condition[]{new Condition()}, 
  10, 0, 0, Action.GRAB, 100, false, false, false, false); AirThrowP2.hitCancel = false;
 AirThrowP2.ActEffs = new ActEffect[]{new GrabEff(xyGrab1 , new int[]{10,2}), new PutInHitSt(30, 10, new ActTimeCond2(0, 18) ), new OppEff(new SetForce(-4, 20, 1), 1, new ActTimeCond2(0,18) ) };

  AirThrow = new Action("FH-airthrow", 
  new Animation( new int[]{2, 2, 6, 4, 4, 2}, 20, 40, 6, "FHouse/FH-airThrow/FH-airThrow"), 
 new Condition[]{new facingCond(1), new fPButCond(8), new InAir()}, 
  10, 0, 0, Action.NOTHING, 10, true, true, false, false);
 AirThrow.ActEffs = new ActEffect[]{new ChangeActTo(AirThrowP2, 0, new OppCheck( new InAir() ) )};
 AirThrow.hitCancel = true;
 
 Falling = new Action(new Animation(new int[]{4, 4, 4}, 0, 3, "FHouse/FH-jumpfall/FH-jfall"), 0, 0, 0, 0, true, true, false, false);
 Falling.updFrameDataArr(0, 0); Falling.updFrameDataArr_float(0, 0, 0);
 Falling.HurtBoxCollect.get(0).add(new ColCircle(0, -100, 180, 180));
 Falling.resetAnim = false; //Falling.AttAnim.loop = false;
 //Falling.ActEffs = new ActEffect[]{new FirstTimeEff(new ResAnimEff(1))};
 
 Landing = new Action(new Animation(new int[]{3}, 0, 1, "FHouse/FH-sttocr/FH-sttocr"), 0, 0, 0, 0, true, true, false, false);
 Landing.updFrameDataArr(0, 3); Landing.updFrameDataArr_float(0, 0, 0);
 Landing.HurtBoxCollect.get(0).add(new ColCircle(0, -100, 180, 180));
 Landing.resetAnim = false; Landing.attWeight = 0;
 
 Turning = new DirChangeAct("FH-st-idle", new Animation(new int[]{3,3,3},  0, 3, "FHouse/FH-turning/FH-turning"),new Condition[]{}, 0, 0, 0, 0, 0, true, false, false, false);
 Turning.updFrameDataArr(0, 9); Turning.updFrameDataArr_float(0, 0, 0);
 Turning.HurtBoxCollect.get(0).add(new ColCircle(0, -100, 180, 180));
 Turning.attRank = Action.HITSTATE;//Turning.ActEffs = new ActEffect[]{};
  
  specSetup();
  
  }
  
  abstract void specSetup();
  
  void standingStateReturn(Fighter Opp){
    if(y == GROUNDHEIGHT || CollisionBox.bottom) curAirActions = maxAirActions;
    
      if((y >= GROUNDHEIGHT || CollisionBox.bottom) && (CurAction.attKind == 4 || CurAction == Falling) ){
        changeAction(Landing); Falling.AttAnim.Reset();
      }
      else if((y == GROUNDHEIGHT || CollisionBox.bottom) && inputs[1]){
        if(CurAction == Standing) changeAction(Landing);
        else CurAction.playAction2(this, Opp, Crouching);
      }
      else if (y == GROUNDHEIGHT || CollisionBox.bottom){
        if(CurAction == Crouching) changeAction(Landing);
        else CurAction.playAction2(this, Opp, Standing);
      }
      else if(y < GROUNDHEIGHT || !CollisionBox.bottom){
        CurAction.playAction2(this, Opp, Falling);
      }
      
  }
  
  protected void gameLogic(Fighter Opp){
    if(Recorder != null){ 
      if(Recorder.state != 0 ) Recorder.work(this);
      if(Recorder.state != 2) PlContr.deviceInput();
      PlContr.draw();
    }
      
    for(int i = 0; i < PlContr.inputs.length;i++) this.inputs[i] = PlContr.inputs[i];
    
    if(AI_Controlled){ 
      if(frame % 6 == 0){      
        AI_Controll(Opp);
        AIControll2(Opp);
      }
      AIControll3(Opp);
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
    controll(Opp);
   
    updateColList();
    updateProjectiles(Opp);
    
    if(CurAnimation != null){
      CurAnimation.handleAnim();
        }
    
    
        curHP = constrain(curHP, 0, maxHP);
    curSuper = constrain(curSuper, 0, maxSuper);

    if(cancelWindow > 0){
      cancelWindow--;
    }
    
   extraStuff();
  }
 
  protected void draw(){
    
   sprite();
        
    pushMatrix();
    translate(x, y);
    if(facing == LEFT){
      scale(-1, 1);
    }
    
    if(tint) tint(240, 153, 255);
    if(HurtBoxes.size() == 0 && frame % 4 == 0) tint(105, 105, 105, 100);
    //if(throwInvu > 0 && frame % 4 == 3) tint(105, 205, 105, 100);
    imageMode(CENTER);
    if(CurAnimation != null){
      
    if(tint) tint(255, 153, 150);
    if(HurtBoxes.size() == 0 && frame % 4 == 0) tint(105, 105, 105, 100);
    //if(throwInvu > 0 && frame % 4 == 3) tint(195, 255, 195, 100);
    rotate(CurAnimation.rot);
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
    if(Recorder != null && Recorder.state != 0 ){
      Recorder.work(this);
    }
    if(device != null && Recorder.state != 2){
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
      inputs[8] = EMBut.pressed();
    }
    
    
  }
  
  void keyPressed(){
    //PlContr.keyReleased();
    inputs = inputsKey(true, PlContr.charinputs, PlContr.inputs);
    //for(int i = 0; i < PlContr.inputs.length;i++) this.inputs[i] = PlContr.inputs[i];
  }
  void keyReleased(){
    if(!AI_Controlled) //PlContr.keyReleased();
    inputs = inputsKey(false, PlContr.charinputs, PlContr.inputs);
    //for(int i = 0; i < PlContr.inputs.length;i++) this.inputs[i] = PlContr.inputs[i];
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
 
  
void fighterActionsExtra(){}

void fighterActions(Fighter Opp){
      if((CurAction == Standing || CurAction == bWalk|| CurAction == fWalk || (CurAction.hitCancel && cancelWindow > 0 && !inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){  
        if(cancelWindow <= 0 && !inputs[1]){ // not able to walk while holding down
           normalWalk();
        }
           jump();
           dash();    
           st_Normals();
           checkSingleActList(Opp, ActionList.get(0));
           easyControl(0);
       }
       else if( (CurAction == Crouching || (CurAction.hitCancel && cancelWindow > 0 && inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){
         cr_Normals();
         checkSingleActList(Opp, ActionList.get(1));
         easyControl(0);
       }
       else if( (CurAction == Falling || (CurAction.hitCancel && cancelWindow > 0) || CurAction.allButSelfCancel ) && y < GROUNDHEIGHT && !CollisionBox.bottom){
         airDash();
         jump();
         j_Normals();
         checkSingleActList(Opp, ActionList.get(2));
         easyControl(2);
       }
       else if(CurAction.attRank < 1){
         checkSingleActList(Opp, ActionList.get(3));
         //easyControl(3);
       }
       
       fighterActionsExtra();
       

       
}

void easyControl(int z){
       if(easyMode){
                  int inputNum = 0; Action[] easyActs = new Action[7];
           for(int i = 0; i < ActionList.get(z).size() && inputNum < easyActs.length; i++){
             Action a = ActionList.get(z).get(i);
             Condition[] c = a.Conds;
             for(int j = 0; j < c.length; j++){
               if( (c[j] instanceof comfPButC || c[j] instanceof dirCombCond || c[j] instanceof OrCond || c[j] instanceof ChargeDirCheck) && inputNum < easyActs.length){
                 easyActs[inputNum] = a;
                 inputNum++;
                 break;
               }
             }
           }
           
           for(int i = 0; i < 7; i++){ if(inputs[8] && inputs[i] && easyActs[i] != null) changeAction(easyActs[i]); }
           
       }
}

void st_Normals(){}; void cr_Normals(){}; void j_Normals(){};


void normalWalk(){
      if(inputs[2]){
      if(dirMult == 1){
        CurAction.reset();
        CurAction = fWalk;
        curSuper++;
      }
      else if(dirMult == -1){
        CurAction.reset();
        CurAction = bWalk;
      }
    }
    
    if(inputs[3]){
       if(dirMult == -1){
        CurAction.reset();
        CurAction = fWalk;
        curSuper++;
      }
      else if(dirMult == 1){
        CurAction.reset();
        CurAction = bWalk;
      }
    }
}
void jump(){
  if(cancelWindow <= 0 || CurAction.jumpCancel) ; else return;
  int[] fJump = {9};
  int[] bJump = {7};
  Action l_ResultAction = CurAction;
  if(y == GROUNDHEIGHT || CollisionBox.bottom){
    if(compareBufferWithCombAtt(fJump) && inputs[0]){
      l_ResultAction = fDiaJump;
    }
    else
    if(compareBufferWithCombAtt(bJump) && inputs[0]){
      l_ResultAction = bDiaJump;
    }
    else
      if(inputs[0]
      ){
      l_ResultAction = Jumping;
    }
    
  }
  else
  if(curAirActions > 0){
        if(compareBufferWithCombAtt(fJump) && firstPressInp[0]){
      l_ResultAction = fDiaJump2;
    }
    else
    if(compareBufferWithCombAtt(bJump) && firstPressInp[0]){
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
       
       if(cancelWindow > 0) cancelWindow = 0;
    }
  
}
void dash(){
  if(cancelWindow <= 0 || CurAction.dashCancel) ; else return;
  if(curAirActions > 0){
          if(compareBufferWithCombAtt(fDashmotion) && CurAction != FDash){
            CurAction.reset();
          CurAction = FDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
    else
    if(compareBufferWithCombAtt(bDashmotion) && CurAction != BDash){
            CurAction.reset();
          CurAction = BDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
  }
}

void airDash(){
  if(airFDash == null || airBDash == null){dash(); return;}
  
    if(cancelWindow <= 0 || CurAction.dashCancel) ; else return;
  if(curAirActions > 0){
    if(compareBufferWithCombAtt(fDashmotion) && CurAction != airFDash){
            CurAction.reset();
          CurAction = airFDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
    else
    if(compareBufferWithCombAtt(bDashmotion) && CurAction != airBDash){
            CurAction.reset();
          CurAction = airBDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
  }
}

  void faultlessDefense(){
                    //faultless Defense //TODO: Auslagern von Blockaktion in allgemeine Actlist
    if( (CurAction == Standing || CurAction == Falling || CurAction == Blocking || CurAction == Crouching || CurAction == bWalk) && inputs[8] 
    && ( (inputs[2] && dirMult == -1) || inputs[3] && dirMult == 1 ) && curSuper > 0){ 
      if(inputs[1]) Blocking.AttAnim = LBlock; else Blocking.AttAnim = HBlock;
      Blocking.whenToUpdBoxs[0] = 1; changeAction(Blocking);
      VisEffectsList.add(new VisualEffect(x, y - 100, BurstEff, dirMult)); curSuper -= 4;
  }
  
  }
  
  void controll(Fighter Opp){ 

  
        if(HitStunT == 0 && CurAction != Blocking && CurAction != BeingGrapped && CurAction != InHitStun && CurAction.attRank != Action.HITSTATE){
  
       fighterActions(Opp);
       
    faultlessDefense();
       
    if( CurAction != Standing && inputs[7] && firstPressInp[7] && curSuper >= 500){
      changeAction(Standing);
      curSuper -= 500; slowMoDur = 40; slowMoValue = 4;
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
                changeAction(KDstandup);
                      Soundeffects[1].cue(0);
      Soundeffects[1].play();
      VisEffectsList.add(new VisualEffect(x, y, RCEff, 0));
                break;
              }
    }
    }
    
            if(CurAction == softKD && softKD.curCollumn == 1 && !new Grounded().cond(this, Opp)){
      for(int i = 0; i < inputs.length; i++){
              if(inputs[i] && firstPressInp[i]){
                changeAction(Standing);
                gforce = 0; Force.y = 0;
                if(inputs[0]){Force.y = -18;}
                if(inputs[1]){Force.y = 12;}
                if(inputs[2]){Force.x = 16;}
                if(inputs[3]){Force.x = -16;}
                slowMoDur = 9; slowMoValue = 3;
                      Soundeffects[1].cue(0);
      Soundeffects[1].play();
      VisEffectsList.add(new VisualEffect(x, y, RCEff, 0));
                break;
              }
    }
    }
    else if(CurAction == softKD && softKD.curCollumn == 1 && new Grounded().cond(this, Opp)){
                changeAction(Standing);
    }
    
    if(throwInvu > 0) throwInvu--;
    //if(CurAction == InHitStun || CurAction == Blocking || CurAction == softKD || CurAction == Knockdown || CurAction == KDstandup || CurAction == BeingGrapped){}
        
    if(CurAction.gravityActive && !CollisionBox.bottom && y < GROUNDHEIGHT){
      Force.y += gforce;
    }
    else{gforce = 0;}
    
    if( y < GROUNDHEIGHT && CurAction.gravityActive && !CollisionBox.bottom && gforce <= 2.5 //&& cancelWindow <= 0
    ){
      //gforce += 0.2 * m + 0.01;
      gforce += abs(Force.y) * 0.01 * m + 0.02  ;
      gforce *= CurAction.gravMult;
    }
    else if(y > GROUNDHEIGHT){
      y = GROUNDHEIGHT;
      Force.y = 0;
      gforce = 0;
    }
    
    //Friction
    if(new Grounded().cond(this, this)){
      Force.x = Force.x * 0.95 * CurAction.fricMult;
    }
    else{ Force.x = Force.x * 0.97 * CurAction.fricMult; }
    
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
  
  void updatePos(float[] maxAllowedArr//ArrayList<ColRect> ColRects
  ){
    float maxAllowedFx = maxAllowedArr[0], maxAllowedFy = maxAllowedArr[1];
    ColRect c = CollisionBox; 
    if(-0.1 > Force.x || Force.x > 0.1){
      if(c.rside && maxAllowedFx > 0 && Force.x > 0){
        x += abs(maxAllowedFx)-1;
      }
      else
      if(c.lside && maxAllowedFx < 0 && Force.x < 0){
        x -= abs(maxAllowedFx)-1;
      }
      else
     {x += Force.x;}
      
    }
    /*if(!CollisionBox.bottom ){
      y += Force.y;
    }*/
      if(c.bottom && maxAllowedFy > 0 && Force.y > 0){
        y += abs(maxAllowedFy); Force.y = 0;
      }
      else
      if(c.top && maxAllowedFy < 0 && Force.y < 0){
        y -= abs(maxAllowedFy)-1;
      }
      else
     {y += Force.y;}
    
    updateColList();
    c.setColBools(false);
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
    stroke(200, 0, 0);
    line(x, y, x + Force.x*2, y + Force.y*2);
  }
  
  void drawColList(ArrayList<ColCircle> ColList){
    for(ColCircle c : ColList){
      c.draw(dirMult);
    }
  }
  
  
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
  
  boolean ChargeDirCheck(int chargeDur, int dir){
  
  return (inputChargeT[2] >= chargeDur && dirMult * dir == 1) || (inputChargeT[3] >= chargeDur && dirMult * dir == -1);
}
  
  void facingCheckAndChange(Fighter Opp){
    if(CurAction == Standing || CurAction == Crouching || CurAction == bWalk || CurAction == fWalk){
    if(facing == RIGHT && Opp.x < x ){
      changeAction(Turning);
      //dirMult = -1;
      //facing = LEFT;
      println("left");
          float l = AI_InputsScore[2];
    AI_InputsScore[2] = AI_InputsScore[3];
    AI_InputsScore[3] = l;
      
    }
    else if(facing == LEFT && Opp.x > x ){
      changeAction(Turning);
      //dirMult = 1;
      //facing = RIGHT;
      println("right");
                float l = AI_InputsScore[2];
    AI_InputsScore[2] = AI_InputsScore[3];
    AI_InputsScore[3] = l;
    }
    }

  }
  
  void manualFacing(){
        if((CurAction == Standing || CurAction == Crouching || CurAction == fWalk || CurAction == bWalk) && (CollisionBox.bottom || y == GROUNDHEIGHT)){
          if(dirMult == 1 && inputs[3]){
            changeAction(Turning);
            //dirMult = -1;
            //facing = LEFT;
          }
          if(dirMult == -1 && inputs[2]){
            changeAction(Turning);
            //dirMult = 1;
            //facing = RIGHT;
          }
        }
  }
  
  void extraEffOnHit(){
  }
  
  Action operationsOnHit(Fighter Opp, ColCircle[] l_Boxes){
    Action resultAction = null;
    
    if(l_Boxes != null){
    ColCircle HitBox = l_Boxes[0], c = l_Boxes[1];
    
      boolean instantBlock = false;//instant Block
        if((Opp.inputChargeT[3] < 6 && Opp.dirMult == 1) || (Opp.inputChargeT[2] < 6 && Opp.dirMult == -1)) instantBlock = true;
      boolean FD = false;//faultless Defense
        if( Opp.inputs[8] && Opp.curSuper > 0){ 
          VisEffectsList.add(new VisualEffect(Opp.x, Opp.y - 100, BurstEff, Opp.dirMult)); FD = true;
        }

     if( checkBlock(Opp, CurAction.attKind) && ( (!(new Grounded().cond(this, Opp) && new InAir().cond(Opp, this)) || FD ) )
        ){       
          //Opp.CurAction.reset();
          resultAction = Opp.Blocking;
          
          
          if(Opp.inputs[1] && !new InAir().cond(Opp, this)){
            resultAction.AttAnim = Opp.LBlock;
            resultAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            resultAction.AttAnim = Opp.HBlock;
            resultAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }
          
          if(CurAction.firstHit){
            Opp.Force.x = HitBox.forcex*dirMult + (CurAction.damage/10 + comboCount) * dirMult;
            Opp.Blocking.whenToUpdBoxs[0] = CurAction.affBlockStunT;
            

            if(instantBlock && FD){Opp.Blocking.whenToUpdBoxs[0] = CurAction.affBlockStunT-12;
               VisEffectsList.add(new PopUpMssg(Opp.x, Opp.y-100, "instant FD:"+Opp.inputChargeT[2]+":"+Opp.inputChargeT[3], 20, 0));
            }
            else if(instantBlock){Opp.Blocking.whenToUpdBoxs[0] = CurAction.affBlockStunT-8;
               VisEffectsList.add(new PopUpMssg(Opp.x, Opp.y-100, "instant Block:"+Opp.inputChargeT[2]+":"+Opp.inputChargeT[3], 20, 0));
            }
            
            Opp.throwInvu = CurAction.affBlockStunT+5;
            
            if( new Grounded().cond(this, Opp) ) Force.x =  (CurAction.damage/2) * -dirMult; // changed to prevent certain attacks to catapult to other end of screen
            Force.x += (abs(HitBox.forcex)*2 + CurAction.damage/4) * CurAction.pushBackMult * -dirMult;
            
            if(FD){ Force.x += (abs(HitBox.forcex)*2 + CurAction.damage*2) * -dirMult; Opp.curSuper -= CurAction.damage*4; 
              if(!instantBlock)Opp.Force.x = HitBox.forcex*dirMult + (CurAction.damage/4 + comboCount) * dirMult;
            }
            else if(instantBlock) Force.x += (abs(HitBox.forcex)*2 + CurAction.damage/2) * -dirMult;          
            else Opp.curHP -= CurAction.damage/8;
            
            CurAction.specialEffectOnHit(this, Opp);
            VisEffectsList.add(new VisualEffect(Opp.x + c.addx + c.br/2 * Opp.dirMult, y + HitBox.addy, BlockEff, CurAction.damage/4));
                Soundeffects[4].cue(0);
                Soundeffects[4].play();
                cancelWindow = 6;
              
          }
          
          if(!CurAction.multiHit){
            CurAction.firstHit = false;
          }
          //frameFreeze = CurAction.damage/6;
          slowMoDur = 9; slowMoValue = 3;
  }
  
    else
      if(CurAction != BeingGrapped){
        //counter/punish code hinzufügen
        for(int  i = Opp.CurAction.curCollumn; i < Opp.CurAction.HitBoxCollect.size(); i++){
          if(Opp.CurAction.HitBoxCollect.get(i).size() > 0){
            Opp.counterState = true;
            break;
          }
        }
        
        if(Opp.CurAction == Opp.softKD || Opp.CurAction == Opp.InHitStun ||
        Opp.CurAction == Opp.Knockdown || Opp.CurAction == Opp.Standing){
        Opp.Force.x = HitBox.forcex*dirMult + (CurAction.damage/4 + comboCount) * dirMult;
        Opp.Force.y = HitBox.forcey;
        //Opp.HitStunT = CurAction.affHitStunT;// / (comboCount/2+1);
        Opp.gforce = 0;
        }

        //multihitmoves dont work anymore , only last hit puts into stun
        if(CurAction.firstHit){ 
          
        if( ( HitBox.forcey < 0 || Opp.y+1 < GROUNDHEIGHT || !new Grounded().cond(Opp, Opp) )){
          resultAction = Opp.softKD;
          Opp.softKD.whenToUpdBoxs[0] = CurAction.affHitStunT;
        }
        
        else if(CurAction.knocksDown && Opp.CurAction != Opp.Knockdown && Opp.CurAction != Opp.KDstandup){
          resultAction = Opp.Knockdown;
          Opp.Knockdown.whenToUpdBoxs[0] = CurAction.affHitStunT;
        }
        
        else
        {
          resultAction = Opp.InHitStun;
          Opp.InHitStun.whenToUpdBoxs[0] = CurAction.affHitStunT;
          if(Opp.inputs[1] && !new InAir().cond(Opp, this)){
            resultAction.AttAnim = Opp.LHit;
            resultAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            resultAction.AttAnim = Opp.HHit;
            resultAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }
        }
        Opp.throwInvu = CurAction.affHitStunT + 5;
        
        //handling pushback and such
          
          if( new Grounded().cond(this, Opp) ) Force.x +=  (CurAction.damage/2 + comboCount) * CurAction.pushBackMult * -dirMult; // changed to prevent certain attacks to catapult to other end of screen
   
          Opp.curHP -= CurAction.damage / (comboCount/2+1);
          if(CurAction.superNeed == 0)curSuper += CurAction.damage*8;
          Opp.curSuper += CurAction.damage*4 * comboCount;
          
          CurAction.specialEffectOnHit(this, Opp);
             comboCount++;
             cancelWindow = 6;
             extraEffOnHit();
          VisEffectsList.add(new VisualEffect(Opp.x + c.addx + c.br/2 * Opp.dirMult, y + HitBox.addy, HitEff, CurAction.damage/4));
                Soundeffects[0].cue(0);
                Soundeffects[0].play();
                
         for(ActEffect a : CurAction.ActEffs){
            if(a.whereUsed == 0 && a.cond(this, Opp)){
              a.Effect(this, Opp);
            }}    
        }
        
         for(ActEffect a : CurAction.ActEffs){
            if( a instanceof CounterEff  && a.cond(this, Opp) && !hitstates[a.whereUsed]){
              a.Effect(this, Opp);
              Action[] Acts = {Opp.WallStick, Opp.WallBounce, Opp.Juggle, Opp.GroundBounce, Opp.Stagger};
              resultAction = Acts[a.whereUsed]; hitstates[a.whereUsed] = true;
            }
          }
         
         for(ActEffect a : CurAction.ActEffs){
            if(a.whereUsed == 2 && a.cond(this, Opp)){
              a.Effect(this, Opp);
         }}  
               
        if(!CurAction.multiHit) CurAction.firstHit = false;
        
        //frameFreeze = CurAction.damage/6;
        slowMoDur = 9; slowMoValue = 3;
      }
    }
    return resultAction;
  }
  
  
  ColCircle[] checkHit(Fighter Opp){//0,     1,                2,             3,          4,             5,              6,          7,
    Action[] OppStunStates = {Opp.InHitStun, Opp.BeingGrapped, Opp.Knockdown, Opp.softKD, Opp.WallStick, Opp.WallBounce, Opp.GroundBounce, Opp.Juggle, Opp.Stagger};
    boolean checkST = false; for(Action a : OppStunStates){ 
      if(a == Opp.CurAction){
             checkST = true;}
    }
    if(!checkST){comboCount = 0; Opp.counterState = false; Opp.punishState = false;
      for(int i = 0; i < hitstates.length; i++){hitstates[i] = false;}
    }
     
    ColCircle[] l_Boxes = null;

    for(ColCircle HitBox : HitBoxes){
    for(ColCircle c : Opp.HurtBoxes){
      ColRect OppCb = Opp.CollisionBox; ColCircle l_cGrabBox = new ColCircle(0, 0, OppCb.br, OppCb.ho); l_cGrabBox.setxy(Opp.x, Opp.y-l_cGrabBox.br/2);
      if(CurAction.attKind == Action.GRAB && HitBox.compare(l_cGrabBox, dirMult, Opp.dirMult)){  //Opp Strike check hinzufügen -> throwinvu auf 5 reduzieren
        if(Opp.throwInvu > 0){break;}       
        l_Boxes = new ColCircle[2];
        l_Boxes[0] = HitBox;
        l_Boxes[1] = l_cGrabBox;
          break;
        
      }
      else
      if(CurAction.attKind != Action.GRAB && HitBox.compare(c, dirMult, Opp.dirMult)){
        l_Boxes = new ColCircle[2];
        l_Boxes[0] = HitBox;
        l_Boxes[1] = c;
          
          break;
        }
        
        
      }
    }
 
    //Projektile 
        for(int indexProj = Projectiles.size()-1; indexProj >= 0; indexProj--){
          Projectile p = Projectiles.get(indexProj);
    for(ColCircle c : Opp.HurtBoxes){
      if(p.hitOpp && p.HitBox.compare(c, 1, 1)){  
                if( checkBlock(Opp, p.attKind) 
                ){
          Opp.CurAction.reset();
          //Opp.CurAction = Opp.Blocking;
          Opp.changeAction(Opp.Blocking);
          Opp.Force.x += p.forcex / 4;
          Opp.Force.y += p.forcey / 4;
          Opp.Blocking.whenToUpdBoxs[0] = p.hitStun/2;
          Opp.curHP -= CurAction.damage/10;
           VisEffectsList.add(new VisualEffect(p.x, p.y, BlockEff, p.damage/4));
                Soundeffects[0].cue(0);
                Soundeffects[0].play();
                
                    if(Opp.inputs[1] && !new InAir().cond(Opp, this)){
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
          Opp.softKD.whenToUpdBoxs[0] = p.hitStun;
        }
        else{
          Opp.InHitStun.reset();
          Opp.InHitStun.whenToUpdBoxs[0] = p.hitStun;
          //Opp.CurAction = Opp.InHitStun;
          Opp.changeAction(Opp.InHitStun);
          if(Opp.inputs[1] && !new InAir().cond(Opp, this)){
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
        
            //Armorcheck //TODO:Changing how hit touches are read, maybe also check if opp is in hitstate
    if(l_Boxes != null && Opp.armorCount > 0){
      ColCircle HitBox = l_Boxes[0], c = l_Boxes[1];
      Opp.armorCount--; Opp.curHP -= CurAction.damage/8;
      VisEffectsList.add(new VisualEffect(Opp.x + c.addx + c.br/2 * Opp.dirMult, y + HitBox.addy, BlockEff, CurAction.damage/4));
      Soundeffects[4].cue(0); Soundeffects[4].play();
      l_Boxes = null;
      slowMoDur = 9; slowMoValue = 3;
      CurAction.firstHit = false;
    }
         
        
        return l_Boxes;
      
  }
  
  boolean checkBlock(Fighter Opp, int attKind){
    return ((Opp.facing == LEFT && Opp.inputs[2]) || (Opp.facing == RIGHT && Opp.inputs[3]))
        && (
        ( (Opp.CurAction == Opp.Standing || Opp.CurAction == Opp.Falling || Opp.CurAction == Opp.bWalk)
        && (attKind == Action.HIGH || attKind == Action.AIR || attKind == Action.MID || attKind == Action.NOTHING))
        || 
        ( (Opp.CurAction == Opp.Crouching || (Opp.CurAction == Opp.bWalk && Opp.inputs[1]) ) 
        && (attKind == Action.LOW || attKind == Action.MID || attKind == Action.NOTHING))
        )
        
        ||
        
        (Opp.CurAction == Opp.Blocking 
        && (
        (attKind == Action.MID || attKind == Action.NOTHING)
        ||
        ((!Opp.inputs[1] || new InAir().cond(Opp, this)) && (attKind == Action.HIGH || attKind == Action.AIR) )
        || 
        (Opp.inputs[1] && attKind == Action.LOW )
        ) 
        );
  }
  
  void updateProjectiles(Fighter Opp){
   for(int i = Projectiles.size()-1; i >= 0; i--){
      Projectile p = Projectiles.get(i);
      p.gameLogic(this, Opp);
      
      if(Projectiles.get(i).exTimer == 0){
               if(p.destrEff != null){
                 println("projdie");
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
    if(a == null) return;
    HurtBoxes.clear();
    HitBoxes.clear(); 
    reset();
    CurAction.reset();
    CurAction = a; a.reset();
    a.clearAndCurBoxes(this);
    if(a.AttAnim != null){
      CurAnimation = a.AttAnim;
      CurAnimation.timer = 0; CurAnimation.curCollumn = 0;
    }
    else{ CurAnimation = null;}
  }
  
    void AIControll2(Fighter Opp){
    float aggroLevel = dist(x, y, Opp.x, Opp.y)*0.1 + (Opp.maxHP/(Opp.curHP+1)) - (curHP/(maxHP+1));
    float attChance, moveChance = dist(x, y, Opp.x, Opp.y);
    //Attproperties needed
    //int condAttKind = Action.MID;
    //int condAttTime;
    
    if(whichDirHold(Opp, -1) || Opp.CurAction == Opp.Blocking ){  //Check Enemy Blocking
      if(Opp.inputs[1]){
        //condAttKind = Action.HIGH;
        inputs[1] = false;
        AI_InputsScore[0] += 0.1;
      }
      else{ //condAttKind = Action.LOW; 
         inputs[1] = true; AI_InputsScore[1] += 0.1;}
      
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
    
       
   // when in Hitstun
   if(CurAction == InHitStun){
     inputs[horDir(-1)] = true;
     AI_InputsScore[horDir(-1)] += 0.1;
     if(Opp.CurAction.attKind == Action.LOW){
       inputs[1] = true;
       AI_InputsScore[1] += 0.1;
     }
     else if(Opp.CurAction.attKind == Action.HIGH || Opp.CurAction.attKind == Action.AIR){
              inputs[1] = false;
       AI_InputsScore[1] -= 0.1;
     }
   }
   
   
   
     
  }
  
  
  // "0000 (Dir) 000 (AttButs) 0 (RC) : timeactive"
  
  InputRecord FBlightR = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0110 000 0 : 2",
  "0010 000 0 : 2",
  "0010 100 0 : 2",
  "0010 100 0 : 2"
  );
  InputRecord DPhR = new InputRecord(this, 2, true,
  "0010 000 0 : 2",
  "010 000 0 : 2",
  "0110 000 0 : 2",
  "0010 010 0 : 2",
  "0010 010 0 : 2"
  );
  InputRecord rFBh = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0101 000 0 : 2",
  "0001 000 0 : 2",
  "0001 010 0 : 2",
  "0001 010 0 : 2"
  );
  InputRecord rFBl = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0101 000 0 : 2",
  "0001 000 0 : 2",
  "0001 100 0 : 2",
  "0001 100 0 : 2"
  );
  InputRecord Doublf = new InputRecord(this, 2, true,
  "0010 000 0 : 2",
  "000 000 0 : 1",
  "0010 000 0 : 2",
  "0010 000 0 : 2"
  );

  
  void AIControll3(Fighter Opp){
    float aggro = 0;
    try{
    aggro = dist(x, y, Opp.x, Opp.y)*0.1 + (Opp.maxHP/(Opp.curHP+1)) - (curHP/(maxHP+1));
    }catch(ArithmeticException e){
      aggro = 100;
    }
    float distxy = dist(x, y, Opp.x, Opp.y), distx = dist(x, 0, Opp.x, 0), disty = dist(0, y, 0, Opp.y);
    
    if(distx <320 && disty > 50 && aggro >= 70 && new InAir().cond(this, Opp)){
      Recorder = rFBl;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx <350 && disty > 150 && aggro >= 30){
      Recorder = DPhR;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx <370 && disty > 220 && aggro >= 40){
      Recorder = rFBh;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx > 660 && aggro >= 60){
      Recorder = Doublf;    
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }
    else
    if(distx > 500 && aggro >= 20){
      Recorder = FBlightR;    
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }
    else Recorder = null;

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
             AI_InputsScore[8] += 0.1;
           }
    
   String data = "";
    for(int i = 0; i < AI_InputsScore.length; i++){
      if( random(0, 1) < AI_InputsScore[i] ){
        inputs[i] = true;
      }
      else { inputs[i] = false; }
      
      data += AI_InputsScore[i] + ":";
    }
    //println(data);
    
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
    
    if(firstPressInp[0] && AI_InputsScore[0] > 0.2){
      AI_InputsScore[0] -= 0.1;
    }
    
    abstand = dist(x, y, Opp.x, Opp.y);
    pHP = curHP;
    pOppHP = Opp.curHP;
  }
  
  void drawBars(int mult){
    float centerZ2 = 0 / tan(PI*30.0 / 180.0);
    drawBar( int(Camerabox.x) - mult*(initWidth/4), 20 - int(0) , centerZ2, maxHP, curHP, mult * -180);
    drawBar( int(Camerabox.x) - mult*(initWidth/4), GROUNDHEIGHT+10 - int(0) , centerZ2, maxSuper, curSuper, mult * -140);
  } 
  
}//Ende Hauptklasse

class F_OBHaus extends Fighter{
  F_OBStand Stand; 
  
  
  class F_OBStand extends Fighter{
    F_OBHaus Master; int sumnTimer = 0; final int NOTSUMMONED = 0, SUMMONED = 1, PUPPET = 2; int State = NOTSUMMONED;
    Action Summon, Desummon, Sp1, Sp2Rush, j_downHeavy;
    
   F_OBStand(int x, int y, char[] charinputs, ControlDevice device, F_OBHaus Master){
    super(x,  y, charinputs, device); this.Master = Master;
  }
   F_OBStand(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
  }
  
  void specSetup(){
      name = "OstblockHouse";
   CollisionBox = new ColRect(0, 0, 150, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
      int[] times0 = {5};
      Animation Anim0 = new Animation(times0, 0, 1, "OBHouse/OBS-st/OBS-st");
      Standing = new Action("FH-st-idle", Anim0, false); Standing.gravityActive = true; Standing.addingForce = false;
      Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -240, 280, 180, 0, 0, -1));
          
        int[] times2 = {5};
  Crouching = new Action("fh-cr", new Animation(times2, 0, 1, "OBHouse/OBS-st/OBS-st"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
      
          int[] times14 = {4, 4, 4, 4, 4, 4, 4};
  Condition[] Cond1 = {new fPButCond(5)};    
  Animation Anim14 = new Animation(times14, 100, 7, "OBHouse/OBS-sp1/OBS-1sp");
  LightNormal = new Action("OBS-light", Anim14, Cond1, 20, 6, 12, Action.MID, 1, true, false, false, false);
  
  cr_HeavyNormal = new Action("OBS-cr-heay", 
  new Animation(new int[]{4, 4, 4, 2, 6, 6, 4, 4, 4}, 0, 9, "OBHouse/OBS-cr-heay/OB-cr-heavy"), 
  Cond1, 30, 14, 16, Action.MID, 1, true, false, false, false);
  cr_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.JUGGLE)};
  
  Action HeavyNormal_p2 = new Action("OBS-st-fh2", 
  new Animation(new int[]{5, 4, 4, 4, 4, 5, 2, 4, 2}, 0, 9, "OBHouse/OBS-st-fh/p2/OB-st-fh"), 
  Cond1, 10, 5, 4, Action.HIGH, 1, true, false, false, false);
  
  HeavyNormal = new Action("OBS-st-fh", 
  new Animation(new int[]{2, 2, 2, 2, 5, 2, 8, 2, 100}, 0, 9, "OBHouse/OBS-st-fh/OB-st-fh"), 
  Cond1, 20, 5, 12, Action.HIGH, 1, true, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{new ChangeActTo(HeavyNormal_p2, 1, new Grounded(), new ActTimeCond(3, 1, 3, 100) )};
  HeavyNormal.AttAnim.loop = false;
  
  j_HeavyNormal = new Action("OBS-j-heay", new Animation(new int[]{2, 2, 3, 2,    6, 4, 2, 2}, 100, 8, "OBHouse/OBS-j-heay/OBS-j-heay"), 
  Cond1, 24, 8, 10, Action.HIGH, 1, false, false, false, false);
  j_HeavyNormal.ActEffs = new ActEffect[]{new OBS_setSFToM(Master, 1, new ActTimeCond2(0, 0) )};
  
  j_downHeavy = new Action("OBS-j-dh", new Animation(new int[]{2, 2, 6, 2,    4, 3, 3, 8}, 40, 80, 8, "OBHouse/OBS-j-dH/OBS-j-dH"), 
  new Condition[]{new fPButCond(5), new ButCond(1)}, 18, 8, 10, Action.HIGH, 1, false, false, false, false);
  j_downHeavy.ActEffs = new ActEffect[]{new CounterEff(Action.GROUNDBOUNCE)};
  
    Sp1 = new Action("OBS-sp1", 
  new Animation(new int[]{4, 2, 4, 4, 2, 2, 2, 4, 8, 2, 2, 2, 4, 2}, 280, 14, "OBHouse/OBS-special1snake/OBS-1sp"), 
  Cond1, 10, 5, 4, Action.MID, 1, false, false, false, false);
  Sp1.ActEffs = new ActEffect[]{new changeToOwnXY(620, 0, new ActTimeCond2(4, 8) ) };
  Sp1.AttAnim.loop = false;
     
  Sp2Rush = new Action("OBS-sprush", 
  new Animation(new int[]{2, 4, 2, 4, 4, 4, 4, 4, 4, 2, 2, 2}, 80, 12, "OBHouse/OBS-revRush/OBS-revRush"), 
  Cond1, 20, 11, 4, Action.MID, 1, false, false, false, true);
  //Sp2Rush.ActEffs = new ActEffect[]{new changeToOwnXY(620, 0, new ActTimeCond2(4, 8) ) };
  //Sp2Rush.AttAnim.loop = false;
     
  Summon = new Action("OBS-summon", new Animation(new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2}, 0, 9, "OBHouse/OBS-summon/OBS-summon"), 
new Condition[]{}, 
0, 0, 0, Action.MID, 100, true, false, false, false);

  Desummon = new Action("OBS-desummon", new Animation(new int[]{4, 3, 3, 4, 4}, 0, 5, "OBHouse/OBS-desummon/OBS-desummon"), 
new Condition[]{}, 
0, 0, 0, Action.MID, 100, true, false, false, false);
Desummon.ActEffs = new ActEffect[]{new OBS_setState(Master, NOTSUMMONED, 1, new ActTimeCond2(0, 18) )};

  InHitStun.AttAnim = new Animation(new int[]{2}, 0, 1, "OBHouse/OBS-hitStun/OBS-hitStun");
      
            Action[][] ActTab = { {}, {}, {}, {} };

  fillActionsList(ActTab);
  }
  
 /* void standingStateReturn(Fighter Opp){
    CurAction.playAction2(this, Opp, Standing);
  }*/
  
  void normalWalk(){} void jump(){} void dash(){}
  void st_Normals(){}
  //void st_Normals(){  Force.x += Master.Force.x; Force.y += Master.Force.y; x += Force.x; y += Force.y;}
  //void j_Normals(){  Force.x += Master.Force.x; Force.y += Master.Force.y; x += Force.x; y += Force.y;}
  
  void faultlessDefense(){}
  
  void fighterActionsExtra(){ }
   
  void standingStateReturn(Fighter Opp){
    if(y == GROUNDHEIGHT || CollisionBox.bottom) curAirActions = maxAirActions;
    
        CurAction.playAction2(this, Opp, Standing);
      
  }
   
   void facingCheckAndChange(Fighter Opp){
    if(CurAction == Standing || CurAction == Crouching || CurAction == bWalk || CurAction == fWalk){
    if(facing == RIGHT && Opp.x < x ){
      dirMult = -1;
      facing = LEFT;     
    }
    else if(facing == LEFT && Opp.x > x ){
      dirMult = 1;
      facing = RIGHT;
    }
    }
  }
   
   void gameLogic(Fighter Opp){ 
     facingCheckAndChange(Opp);
     if(State == SUMMONED && CurAction == Standing && sumnTimer < 40) sumnTimer++;
     else if(State == SUMMONED && CurAction != Standing) sumnTimer = 0;
     if(State == SUMMONED && CurAction == Standing && sumnTimer >= 40) changeAction(Desummon);
     
     device = Master.device; if(device != null) setConDevice(); 
     PlContr = Master.PlContr; if(PlContr.device != null) PlContr.setConDevice();
    if(State == NOTSUMMONED){ changeAction(Standing); facingCheckAndChange(Opp); x = Master.x; y = Master.y; return;}
    if(State == SUMMONED){x += Force.x; y += Force.y;//x = Master.x; y = Master.y;
    }
    super.gameLogic(Opp);
    facingCheckAndChange(Opp);
    hitOnStand(Opp);
  }
  
  void extraEffOnHit(){
    Master.comboCount++;
  }
  
 private void hitOnStand(Fighter Opp){
   if(State == NOTSUMMONED)return;
    for(ColCircle OppHitbox : Opp.HitBoxes){
      for(ColCircle StandHurtbox : this.HurtBoxes){
        if(StandHurtbox.compare(OppHitbox, this.dirMult, Opp.dirMult) ){
          /*f(State == SUMMONED){
            InHitStun.whenToUpdBoxs[0] = Opp.CurAction.affHitStunT; 
            changeAction(InHitStun);
            Master.InHitStun.whenToUpdBoxs[0] = Opp.CurAction.affHitStunT; 
            Master.changeAction(Master.InHitStun);
          }
          else */
          if(State == SUMMONED){
            changeAction(Desummon);
            Master.InHitStun.whenToUpdBoxs[0] = Opp.CurAction.affHitStunT; 
            Master.changeAction(Master.InHitStun);
          }
          break;
        }
        
      }
    }
    
    if(Opp instanceof F_OBHaus) {F_OBStand OppStand = ( (F_OBHaus) Opp).Stand;
    if(OppStand.State == NOTSUMMONED || State == NOTSUMMONED)return;
    //Stand-Stand HitCheck
    for(ColCircle StandHitbox : this.HitBoxes){
      for(ColCircle OStandHurtbox : OppStand.HurtBoxes){
        
       if(StandHitbox.compare(OStandHurtbox, this.dirMult, Opp.dirMult) ){
         /*if(OppStand.State == SUMMONED){
          OppStand.InHitStun.whenToUpdBoxs[0] = this.CurAction.affHitStunT; 
          OppStand.changeAction(OppStand.InHitStun);
         }else 
         if(OppStand.State == SUMMONED){
           OppStand.changeAction(OppStand.Desummon);
         }*/
         
          Opp.InHitStun.whenToUpdBoxs[0] = this.CurAction.affHitStunT; 
          Opp.changeAction(Opp.InHitStun);
          break;
        }
        
      }
    }
    
    }
  
  }
  
  
  }//Ende der inneren Standklasse


  Action FarMidNormal, st_fHeavy, j_MidNormal2, j_dH;
  Action Special1, Sp2Snake, Sp3dive, Sp4train, Sp5Rush, Summon;
  
 F_OBHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_OBHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
 F_OBHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
 F_OBHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
 
 F_OBHaus(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
  }
 
 ColCircle[] checkHit(Fighter Opp){
   ColCircle[] ToReturn = super.checkHit(Opp), ToReturn2 = Stand.checkHit(Opp);
   if(Stand.State != Stand.NOTSUMMONED && ToReturn2 != null){ return ToReturn2;} 
   else
   if(ToReturn != null){ return ToReturn;}
      
    return null;
  }
  
  Action operationsOnHit(Fighter Opp, ColCircle[] l_Boxes){
    Action resultAction = Opp.CurAction;
    ColCircle[] ToReturn = super.checkHit(Opp), ToReturn2 = Stand.checkHit(Opp);
    if(Stand.State != Stand.NOTSUMMONED && ToReturn2 != null){return Stand.operationsOnHit(Opp, l_Boxes);} 
     else
    if(ToReturn != null){return super.operationsOnHit(Opp, l_Boxes);}
    return resultAction;
  }
 
 void gameLogic(Fighter Opp){
   //if(Stand.State == Stand.NOTSUMMONED){Stand.changeAction(Stand.Standing); Stand.facingCheckAndChange(Opp); } //xyPos anpassen, nicht nur state
   Stand.gameLogic(Opp);
   super.gameLogic(Opp);
 }
 
 void draw(){
   if(Stand.State != Stand.NOTSUMMONED)Stand.draw();
   super.draw();
 }

 void fighterActionsExtra(){
 }
 
  void keyPressed(){Stand.keyPressed(); super.keyPressed();}
  void keyReleased(){Stand.keyReleased(); super.keyReleased();}
  
void specSetup(){
  Stand = new F_OBStand( (int) x, (int) y, charinputs, device, this);
  Stand.setup();
  name = "OstblockHouse";
   CollisionBox = new ColRect(0, 0, 120, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "OBHouse/OB-st/OB-st");
      Standing = new Action("FH-st-idle", Anim0, false);
      
  Falling.AttAnim = new Animation(new int[]{4}, 0, 1, "OBHouse/OB-jumpfall/OB-jfall");
      
    Summon = new Action("FH-st-idle", Anim0, 
new Condition[]{new OBSH_checkState(this, Stand.NOTSUMMONED, true), new fPButCond(4), new comfPButC(new int[][]{{2, 1, 4}})}, 
0, 0, 0, Action.MID, 100, true, false, false, false);
Summon.ActEffs = new ActEffect[]{new OBS_setStateAndAct(this, Stand.Standing, Stand.NOTSUMMONED, 1)};
  
        
  int[] times1 = {8, 8, 8, 8, 8, 8, 8, 8, 8};
  Animation Anim1 = new Animation(times1, 0, 9, "OBHouse/OB-fwalk/OB-fwalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 3, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2, 0);
  
  int[] times3 = {1, 4, 1, 2, 2};
  Animation Anim3 = new Animation(times3, 0, 5, "OBHouse/OB-jump/OB-jump");
  Anim3.loop = false;
  Jumping = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  Jumping.resetAnim = true;

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  Jumping.addAllLists(1, 2, 0, -20);
  
  fDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  fDiaJump.resetAnim = true;

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
      fDiaJump.addAllLists(1, 2, 11, -20);
  
  bDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  bDiaJump.resetAnim = true;

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bDiaJump.addAllLists(1, 2, -7.5, -20);
  
  Jumping2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  
  FDash = new Action("OB-fdash", new Animation(new int[]{6, 2, 8, 2, 8}, 0, 5, "OBHouse/OB-fdash/OB-fdash"),
  0, 0, 0, 0, false, false, false, false);
  FDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
    
  BDash = new Action("OB-bdash" ,new Animation(new int[]{4, 2, 10, 2, 5}, 0, 5, "OBHouse/OB-bdash/OB-bdash"),
  0, 0, 0, 0, false, false, false, false);
  BDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
  
  airFDash = new Action("OB-afdash", new Animation(new int[]{6, 2, 8, 2, 8}, 0, 5, "OBHouse/OB-fdash/OB-fdash"),
  0, 0, 0, 0, false, false, false, false);
    airFDash.allButSelfCancel = true;

  airBDash = new Action("OB-abdash", new Animation(new int[]{4, 3, 11, 4, 3}, 0, 5, "OBHouse/OB-bdash/OB-bdash"),
  0, 0, 0, 0, false, false, false, false);
  //airBDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
  
    int[] times2 = {5};
  Crouching = new Action("fh-cr", new Animation(times2, 0, 1, "OBHouse/OB-cr/OB-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "OBHouse/OB-stun/OB-hitStunH"); LHit = new Animation(times1, 0, 1, "OBHouse/OB-stun/OB-hitStunL");
  HBlock = new Animation(times1, 0, 1, "OBHouse/OB-stun/OB-blockStunH"); LBlock = new Animation(times1, 0, 1, "OBHouse/OB-stun/OB-blockStunL");
  
  int[] ani9 = {6};
  Blocking = new Action(ani9);
  Blocking.updFrameDataArr(0, 2); 
  Blocking.updFrameDataArr_float(0, 0, 0);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 200, 160));
  
    int[] times14 = {2, 2, 5, 2};
  Condition[] Cond1 = {new fPButCond(4)};    
  Animation Anim14 = new Animation(times14, 0, 4, "OBHouse/OB-st-light/OB-st-light");
  LightNormal = new Action("OBH-st-light", Anim14, Cond1, 12, 5, 4, Action.MID, 1, true, false, false, false);
  //LightNormal.selfCancel = true;
  
  Condition[] Cond2 = {new fPButCond(6), new xDistOpp(160, true)};
    int[] times16 = {5, 2, 8, 5, 4};
    Animation Anim16 = new Animation(times16, 50, 5,"OBHouse/OB-st-clmed/OB-st-clmed");
  MidNormal2 = new Action("OBH-st-clmed", Anim16, Cond2, 20, 12, 12, 3, 3, true, false, false, false);
  MidNormal2.ActEffs = new ActEffect[]{new SnglGatEff(FarMidNormal, new fPButCond(6))};

  FarMidNormal = new Action("OBH-st-fmed", 
  new Animation(new int[]{2, 7, 3, 3, 3, 4, 4}, 50, 7,"OBHouse/OB-st-fmed/OB-st-fmed"), 
  new Condition[]{new fPButCond(6), new xDistOpp(160, false)}, 24, 12, 12, 3, 4, true, false, false, false);
  
  Condition[] Cond3 = {new fPButCond(5)};
    int[] times17 = {5, 2, 2, 10, 4, 4};
    Animation Anim17 = new Animation(times17, 0, 6,"OBHouse/OB-st-summon/OB-st-summon");
  HeavyNormal = new Action("OBH-st-summon", Anim17, Cond3, 16, 12, 18, Action.MID, 5, true, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{ new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)), 
  new OBS_setAction(this, Stand.LightNormal, 1, new ActTimeCond2(2, 1), new OBSH_checkAction(this, true, Stand.Summon, Stand.Standing, Stand.Crouching) )};
  
  int[] times18 = {4, 2, 5, 4};
  Animation Anim18 = new Animation(times18, 25, 4, "OBHouse/OB-cr-light/OB-cr-light");
  cr_LightNormal = new Action("OB-cr-light", Anim18, Cond1, 15, 5, 3, 3, 2, true, false, false, false);
  cr_LightNormal.ActEffs = new ActEffect[]{ new SoundEff("fh-cr-l", 0, 0) };

  int[] times19 = {4, 6, 2, 6, 3, 3, 4, 2};
  Animation Anim19 = new Animation(times19, 50, 8, "OBHouse/OB-cr-med/OB-cr-med");
  cr_MidNormal = new Action("OB-cr-med", Anim19, new Condition[]{new ButCond(6)}, 26, 10, 10, Action.LOW, 4, true, false, true, false);
       
  cr_HeavyNormal = new Action("OBH-st-summon", Anim17, Cond3, 16, 12, 18, Action.MID, 5, true, false, false, false);
  cr_HeavyNormal.ActEffs = new ActEffect[]{new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),  
    new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(1, 1), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.cr_HeavyNormal, 1, new ActTimeCond2(4, 1), new OBSH_checkAction(this, true, Stand.Crouching, Stand.Standing, Stand.Summon) )};
  
  st_fHeavy = new Action("OBH-st-summon", Anim17, 
  new Condition[]{new facingCond(1), new fPButCond(5)}, 
  16, 12, 18, Action.MID, 5, true, false, false, false);
  st_fHeavy.ActEffs = new ActEffect[]{new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)), 
    new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(1, 1), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.HeavyNormal, 1, new ActTimeCond2(4, 1), new OBSH_checkAction(this, true, Stand.Standing, Stand.Crouching, Stand.Summon) ) };
  
  j_HeavyNormal = new Action("OBH-st-summon", Anim17, Cond3, 16, 12, 18, Action.MID, 5, false, false, false, false);
  j_HeavyNormal.ActEffs = new ActEffect[]{ //new OBS_setSFToM(this, 1, new ActTimeCond(0, 0), new OBSH_checkAction(this, Stand.Standing, true)),
    new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkAction(this, true, Stand.Standing)),//, new OBSH_checkState(this, Stand.NOTSUMMONED, true)),  
  new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.j_HeavyNormal, 1, new ActTimeCond2(1, 1), new OBSH_checkAction(this, true, Stand.Summon, Stand.Standing) ),
  new GravEff(0, 0, false), new GravEff(1, 4, true)};
  
    j_dH = new Action("OBH-st-summon", Anim17, new Condition[]{new fPButCond(5), new ButCond(1) }, 16, 12, 18, Action.MID, 5, false, false, false, false);
  j_dH.ActEffs = new ActEffect[]{ //new OBS_setSFToM(this, 1, new ActTimeCond(0, 0), new OBSH_checkAction(this, Stand.Standing, true)),
    new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true), new OBSH_checkAction(this, true, Stand.Standing, Stand.Crouching)),//, new OBSH_checkState(this, Stand.NOTSUMMONED, true)),  
  new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.j_downHeavy, 1, new ActTimeCond2(1, 1), new OBSH_checkAction(this, true, Stand.Summon, Stand.Standing, Stand.Crouching) ),
  new GravEff(0, 0, false), new GravEff(1, 4, true)};

  int[] times20 = {5, 2, 5, 3};
  Animation Anim20 = new Animation(times20, 20, 50, 4, "OBHouse/OB-j-light/OB-j-light");
  j_LightNormal = new Action("OB-j-l", Anim20, Cond1, 12, 4, 3, Action.AIR, 1, true, true, false, false);
  j_LightNormal.selfCancel = true;
  //j_LightNormal.ActEffs = new ActEffect[]{};
  
  int[] times22 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  j_MidNormal = new extraForceOnHitAct( "OB-j-m", new Animation(times22, 20, 120, 10, "FHouse/FH-j.med/HF-j.med"),
                               16, 8, 10, Action.AIR, true, true, false, false, 3, -16);
            Condition[] Cond20 = {new ButCond(6), new ButCond(1)};
            j_MidNormal.Conds = Cond20; j_MidNormal.attWeight = 4;
  //j_MidNormal.ActEffs = new ActEffect[]{};
                               
  int[] times28 = {2, 4, 2, 6, 2, 2};
  j_MidNormal2 = new Action( "OB-j-m", new Animation(times28, 20, 70, 6, "OBHouse/OB-j-med/OB-j-med"), new Condition[]{new ButCond(6)},
                               14, 8, 8, Action.AIR, 3, true, true, false, false);
  //j_MidNormal2.ActEffs = new ActEffect[]{};
                             
  
  int[] times24 = {2, 2, 2, 2, 4, 2, 2, 2};
  int[][] DPvars = {{6, 2, 3}, {6, 3, 2, 3, 6}, {6, 3, 2, 3}, {6, 2, 6}};
  Condition[] Cond4 = {new comfPButC(DPvars), new fPButCond(5)};
  Special1 = new Action("FH-sp1", new Animation( times24, 50, 8, "FHouse/FH-sp1/FH-1sp"), Cond4, 18, 16, 6, Action.MID, 20, true, true, true, false);
  Special1.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, true), new ForceAddEff(8, 0, false), new ForceAddEff(12, 0, false)};
  
    
  Sp2Snake = new Action("OBH-st-summon", Anim17, 
  new Condition[]{new fPButCond(5), new comfPButC(new int[][]{ {2, 3, 6} } )}, 
  16, 12, 18, Action.MID, 7, true, false, false, false);
  Sp2Snake.ActEffs = new ActEffect[]{new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),  
    new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(1, 1), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.Sp1, 1, new ActTimeCond2(4, 1), new OBSH_checkAction(this, true, Stand.Standing, Stand.Summon) )};
  
    Action Sp3diveP3 = new Action("FH-j-light", new Animation(new int[]{2, 2, 2}, 0, 0, 3, "OBHouse/OB-gdive/p3/OB-gdive"), 
  new Condition[]{}, 
  12, 4, 3, Action.MID, 1, true, false, false, false);
  
    Animation Sp3neutral = new Animation(new int[]{2}, 0, 0, 1, "OBHouse/OB-gdive/p2/OB-gdive"),
  Sp3forward = new Animation(new int[]{2}, 0, 0, 1, "OBHouse/OB-gdive/p2/OB-gdivefor"), 
  Sp3back = new Animation(new int[]{2}, 0, 0, 1, "OBHouse/OB-gdive/p2/OB-gdiveback"); 
  Sp3neutral.loop = true; Sp3forward.loop = true; Sp3back.loop = true;
    Action Sp3diveP2 = new Action("OBH-sp2p2", Sp3forward, 
  new Condition[]{ new ButCond(6) }, 
  12, 4, 3, Action.MID, 1, true, false, false, false); Sp3diveP2.collision = false; Sp3diveP2.resetAnim = false;
  Sp3diveP2.ActEffs = new ActEffect[]{ new ChangeAnimTo(Sp3forward, 1, new facingCond(1)), new ChangeAnimTo(Sp3back, 1, new facingCond(-1)), new ChangeAnimTo(Sp3neutral, 1, new FalseCond(new facingCond(1), new facingCond(-1))),
  new SetForce(14, 0, 1, new facingCond(1)), new SetForce(-14, 0, 1, new facingCond(-1)), new ChangeActTo(Sp3diveP3, 1, new FalseCond(new ButCond(6)) ),
  new ChangeActTo(Sp3diveP3, 1, new ActTimeCond2(0, 79))};
  
  Sp3dive = new Action("FH-j-light", new Animation(new int[]{2, 2, 2, 2, 2}, 0, 0, 5, "OBHouse/OB-gdive/OB-gdive"), 
  new Condition[]{new fPButCond(6), new comfPButC(new int[][]{ {2, 1, 4} } )}, 
  12, 4, 3, Action.MID, 50, true, false, false, false);
  Sp3dive.ActEffs = new ActEffect[]{ new ChangeActTo(Sp3diveP2, 1, new ActTimeCond2(1, 2) ) };
  
   Projectile TrainProj = new Projectile(0.0, 0.0, 50, -10, 160, 60, 10.0, 0.0, 1, 120, 20, 10, 10, false, false, false, true, true) ;
   TrainProj.setAnims(new Animation(new int[]{10, 10, 10, 10}, 0, -50, 4, "Projectiles/Train/Train"), new Animation(new int[]{10, 10, 10, 10}, 0, -25, 4, "Projectiles/Train/Train"));
                  int[] times32 = {4, 4, 2, 6, 6,
                2, 6, 2, 6, 4};
         Condition[] Cond8 = {new fPButCond(4), new dirCombCond(new int[]{2,3,6})};
   Sp4train = new Action("OB-trainFB",
   new Animation( times32, 50, 10, "OBHouse/OB-trainFB/OB-trainFB"), Cond8, 32, 10, 4, Action.MID, 18, true, false, false, false);
   Sp4train.ActEffs = new ActEffect[]{ new ProjAddEff( TrainProj, 1, new ActTimeCond2(1, 1) )};
   
     Sp5Rush = new Action("OBH-st-summon", Anim17, 
  new Condition[]{new fPButCond(5), new comfPButC(new int[][]{ {2, 1, 4} } )}, 
  16, 12, 18, Action.MID, 7, true, false, false, false);
  Sp5Rush.ActEffs = new ActEffect[]{new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),  
    new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(1, 1), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.Sp2Rush, 1, new ActTimeCond2(4, 1), new OBSH_checkAction(this, true, Stand.Standing, Stand.Summon) )};
   
  LightNormal.addEffs(new GatlingEff(MidNormal2), new GatlingEff(new Condition[]{new ButCond(1)}, cr_LightNormal));
  MidNormal2.addEffs(new GatlingEff(new Condition[]{new ButCond(1)}, cr_HeavyNormal, cr_MidNormal2), new GatlingEff( HeavyNormal));
  FarMidNormal.addEffs(new GatlingEff(new Condition[]{new ButCond(1)}, cr_HeavyNormal, cr_MidNormal2), new GatlingEff( HeavyNormal));
  //HeavyNormal.addEffs(new GatlingEff());
  cr_LightNormal.addEffs(new GatlingEff(LightNormal, MidNormal2), new GatlingEff(new Condition[]{new ButCond(1)}, cr_MidNormal));
  //cr_MidNormal2.addEffs(new GatlingEff(HeavyNormal), new GatlingEff(new Condition[]{new ButCond(1)}, cr_MidNormal, cr_HeavyNormal));
  //cr_HeavyNormal.addEffs(new GatlingEff(MidNormal2), new GatlingEff(new Condition[]{new ButCond(1)}));
  j_LightNormal.addEffs(new GatlingEff(j_MidNormal2));
  j_MidNormal.addEffs(new GatlingEff(j_LightNormal, j_HeavyNormal, j_DustNormal));
 // j_DustNormal.addEffs(new GatlingEff(j_MidNormal2, j_HeavyNormal));
   
    Action[][] ActTab = {
  {st_fHeavy, LightNormal, FarMidNormal, MidNormal2, HeavyNormal, FDash, BDash, Jumping, fDiaJump, bDiaJump, Special1, Summon, Sp2Snake, Sp3dive, Sp4train, Sp5Rush, NormalThrow},
  {cr_LightNormal, cr_MidNormal, cr_HeavyNormal, Special1},
  {j_LightNormal,j_MidNormal2, j_MidNormal, j_dH, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2, NormalThrow, Special1, Sp2Snake, AirThrow},
  {Special1, Summon, Sp2Snake, Sp3dive, Sp4train, Sp5Rush}
};

  fillActionsList(ActTab);
   
}
  void st_Normals(){
    
  }
  
  void cr_Normals(){
    /*if(inputs[6] && Stand.State == Stand.NOTSUMMONED){ Stand.State = Stand.SUMMONED; Stand.changeAction(Stand.Summon);}
    else if(inputs[6] && Stand.State == Stand.SUMMONED) Stand.changeAction(Stand.Desummon);
    
    if(inputs[5] && Stand.State == Stand.SUMMONED ) Stand.State = Stand.PUPPET;
    else if(inputs[5] && Stand.State == Stand.PUPPET) Stand.State = Stand.SUMMONED;*/
  }
  
  void j_Normals(){
    }   
         
}

//FAMILIENHAUS###############################################################################################################################################################################################
class F_FHaus extends Fighter{

  Action st_fHeavy, j_MidNormal2;
  Action BaseNormal, RoofNormal;
  ProjAction Fireball, Fireball2;
  Action Special1, Special3, Special4, Super1;
  Animation FB1AnimUp, FB1AnimMid, FB1AnimDown, BettDestrEff;
  
  /*  Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, st_fHeavy, BaseNormal, RoofNormal, FDash, BDash, Jumping, fDiaJump, bDiaJump, Super1},
  {cr_LightNormal, cr_MidNormal, cr_HeavyNormal, Super1},
  {j_LightNormal, j_MidNormal, j_MidNormal2, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2},
  {Special1, Special3, Special4, Fireball, Fireball2, Super1}
};*/
  
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
  
 F_FHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
 
 F_FHaus(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
 }
  
void specSetup(){
  name = "FamilyHouse";
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
      Standing.resetAnim = false;
      //Standing.ActEffs = new ActEffect[]{new ChangeAnimTo(jFallAnim, 1, new InAir(), new FalseCond(new AnimCheck(jFallAnim)) ), new ChangeAnimTo(Anim0, 1, new Grounded())};
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
  
  Falling.AttAnim = new Animation(new int[]{4, 4, 4}, 0, 3, "FHouse/FH-jumpfall/FH-jfall"); Falling.resetAnim = false; Falling.AttAnim.loop = false;
        
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, 0, 7, "FHouse/FH-fWalk/FH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 4, 0);//3, 0);
  
  bWalk = new Action(new Animation(new int[]{10,8,8,10,10,10}, 0, 6, "FHouse/FH-bWalk/FH-bwalk"), false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2.5, 0);
  
  int[] times3 = {1, 2, 1, 4};
  Animation Anim3 = new Animation(times3, 0, 4, "FHouse/FH-n.jump/FH-n.jump");
  Anim3.loop = false;
  Jumping = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  Jumping.resetAnim = true;

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  Jumping.addAllLists(1, 2, 0, -20);
  
  fDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  fDiaJump.resetAnim = true;

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
      fDiaJump.addAllLists(1, 2, 11, -20);
  
  bDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  bDiaJump.resetAnim = true;

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bDiaJump.addAllLists(1, 2, -7.5, -20);
  
  Jumping2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  
  //FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  FDash = new Action("FH-st-fdash", new Animation(new int[]{6, 2, 8, 4, 8}, 0, 5, "FHouse/FH-st-fdash/FH-st-fdash"),
  0, 0, 0, 0, false, false, false, false);
  FDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
   /* FDash.updFrameDataArr(0, 6);
    FDash.updFrameDataArr_float(0, 0, 0);
    FDash.addAllLists(1, 12, 20, 0);
    FDash.addAllLists(2, 2, 18, 0); *///function adds zero length collumn at end
    
  //BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  BDash = new Action("FH-st-bdash" ,new Animation(new int[]{4, 8, 6, 4}, 30, 4, "FHouse/FH-st-bdash/FH-st-bdash"),
  0, 0, 0, 0, false, false, false, false);
  BDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
  BDash.updFrameDataArr(0, 4);
  BDash.updFrameDataArr_float(0, 0, 0);
  BDash.addAllLists(1, 8, -10, 0);
  BDash.addAllLists(2, 10, -2.2, 0);
  
  //FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  airFDash = new Action("FH-st-aifdash", new Animation(new int[]{6, 2, 8, 4, 8}, 0, 5, "FHouse/FH-st-fdash/FH-st-fdash"),
  0, 0, 0, 0, false, false, false, false);
  //airFDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
   /* airFDash.updFrameDataArr(0, 6);
    airFDash.updFrameDataArr_float(0, 0, 0);
    airFDash.addAllLists(1, 10, 23, 0);
    airFDash.addAllLists(2, 1, 23, 0);*/
    airFDash.allButSelfCancel = true;
    

  //BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  airBDash = new Action("FH-st-bdash", new Animation(new int[]{4, 8, 6, 4}, 30, 4, "FHouse/FH-st-bdash/FH-st-bdash"),
  0, 0, 0, 0, false, false, false, false);
  //airBDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
  airBDash.updFrameDataArr(0, 4);
  airBDash.updFrameDataArr_float(0, 0, 0);
  airBDash.addAllLists(1, 8, -14, 0);
  airBDash.addAllLists(2, 10, -4.2, 0);
  
  int[][] FBvars = {{2, 3, 6}, {2, 3, 6, 9}};
  Condition[] CondFB0 = {new InAir(), new comfPButC(FBvars), new fPButCond(4)};
  int [] times7 = {4, 4, 4, 4, 3, 8, 11, 11};
  Animation Anim = new Animation(times7, 0, 8, "FHouse/FH-sp2-gr/FH-2sp-gr");
  Fireball = new FHaus_ProjAction(Anim, 20, 15, 12, 0, false, false, false, false, 1.99, 80, 5.4, 0, true, true, false, FB1AnimUp, FB1AnimMid, FB1AnimDown);
  Fireball.Conds = CondFB0; Fireball.attWeight = 30; Fireball.gravMult = 1.05;
  Fireball.ActEffs = new ActEffect[]{new GravEff(0, 0, false), new GravEff(1, 2, true)};
  Fireball.updFrameDataArr(0, 19);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball.addAllLists(1, 25, 0, 0);
  Fireball.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball.ProjAnim = FB1AnimMid;
  Fireball.destrEffAnim = BettDestrEff;
  
  Condition[] CondFB1 = {new Grounded(), new comfPButC(FBvars), new fPButCond(4)};
  Fireball2 = new FHaus_ProjAction(Anim, 20, 15, 12, 0, false, false, false, false, 1.8, 80, 5.4, -5, true, true, false, FB1AnimUp, FB1AnimMid, FB1AnimDown);
  Fireball2.Conds = CondFB1; Fireball2.attWeight = 30;
  Fireball2.updFrameDataArr(0, 19);
  Fireball2.updFrameDataArr_float(0, 0, 0);
  Fireball2.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball2.addAllLists(1, 35, 0, 0);
  Fireball2.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball2.ProjAnim = FB1AnimMid;
  Fireball2.destrEffAnim = BettDestrEff;
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action("fh-cr", new Animation(times2, 0, 4, "FHouse/FH-cr/HF-cr"), false);
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

  
  /*int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD");
  //softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-hKD/FH-hKD");*/

  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  
    int[] times14 = {3, 2, 5, 2, 3};
  Condition[] Cond1 = {new fPButCond(4)};    
  Animation Anim14 = new Animation(times14, 0, 5, "FHouse/FH-st.Light/FH-st.Light");
  LightNormal = new Action("FH-st-light", Anim14, Cond1, 10, 6, 4, Action.MID, 1, true, false, false, false);
      LightNormal.ActEffs = new ActEffect[]{new SoundEff( "fh-st-l" , 0, 0), //new SelfCancEff(LightNormal), 
      }; 
      
  Condition[] Cond2 = {new fPButCond(6)};
    int[] times16 = {2, 2, 3, 2, 2, 5, 2, 1, 1, 2};
    Animation Anim16 = new Animation(times16, 50, 10,"FHouse/FH-st.Mid/FH-st.Mid");
  MidNormal2 = new Action("FH-st-med", Anim16, Cond2, 20, 12, 12, 3, 1, true, false, false, false);
  MidNormal2.ActEffs = new ActEffect[]{new SoundEff("swoosh_5", 0, 1)};
  
  Condition[] Cond3 = {new fPButCond(5)};
    int[] times17 = {2, 2, 3, 2, 4, 6, 2, 1, 1, 2, 2, 2};
    Animation Anim17 = new Animation(times17, 50, 12,"FHouse/FH-st.Heavy/FH-st.Heavy");
  HeavyNormal = new Action("FH-st-heay", Anim17, Cond3, 16, 12, 18, Action.MID, 1, true, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_0", 0, 1), new SoundEff("honk_0", 1, 1), new CounterEff(0, new CounterHit() )};
  
  int[] times18 = {2, 3, 5, 3, 2};
  Animation Anim18 = new Animation(times18, 25, 5, "FHouse/FH-cr.Light/FH-cr.Light");
  cr_LightNormal = new Action("FH-cr-light", Anim18, Cond1, 15, 6, 3, 3, 1, true, false, false, false);
  cr_LightNormal.ActEffs = new ActEffect[]{ new SoundEff("fh-cr-l", 0, 0)};
  
  int[] times19 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  Animation Anim19 = new Animation(times19, 50, 15, "FHouse/FH-cr.Mid/FH-cr.Mid");
  cr_MidNormal = new Action("FH-cr-med", Anim19, new Condition[]{new fPButCond(8)}, 26, 10, 10, Action.LOW, 1, true, true, true, false);
  cr_MidNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_2", 2, 1)};
  

  cr_MidNormal2 = new Action("FH-cr-m2", new Animation(new int[]{4, 3, 1, 2, 4, 1, 2, 2, 4, 2, 2}, 50, 11, "FHouse/FH-cr-m2/FH-cr-2m"), 
  Cond2, 18, 8, 8, Action.MID, 1, true, false, false, false); cr_MidNormal2.jumpCancel = true;
  cr_MidNormal2.ActEffs = new ActEffect[]{new SoundEff("swoosh_2", 2, 1), new OppEff(new SetForce(4, -5, 2), 2, new OppCheck(new InAir()), new ActTimeCond3(3, 8) ), new FirstHitEff(3, 2, true) };
  
    int[] times23 = {2, 4, 2, 2, 3, 3, 5, 2, 3, 2, 2, 2};
  cr_HeavyNormal = new Action( "FH-cr-heay", new Animation(times23, 30, 12, "FHouse/FH-cr.Heavy/HF-cr.Heavy"), Cond3,
                               16, 8, 16, Action.MID, 1, true, false, false, false);
       cr_HeavyNormal.ActEffs = new ActEffect[]{new SoundEff(7, 0, 0), new CounterEff(2, new CounterHit() )};
       cr_HeavyNormal.jumpCancel = true;
  
  int[] times20 = {2, 1, 4, 1, 2};
  Animation Anim20 = new Animation(times20, 25, 50, 5, "FHouse/FH-j.Light/FH-j.Light");
  j_LightNormal = new Action("FH-j-light", Anim20, Cond1, 12, 4, 3, Action.AIR, 1, true, true, false, false);
  //j_LightNormal.selfCancel = true;
  j_LightNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_2", 1, 1)};
    
  int[] times22 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  j_MidNormal = new extraForceOnHitAct( "FH-j-med", new Animation(times22, 0, 90, 10, "FHouse/FH-j.med/HF-j.med"),
                               16, 8, 10, Action.AIR, true, true, false, false, 3, -16);
            Condition[] Cond20 = {new ButCond(6), new ButCond(1)};
            j_MidNormal.Conds = Cond20; j_MidNormal.attWeight = 2;
            j_MidNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_4", 0, 0), new CounterEff(3)};
                               
  int[] times28 = {2, 3, 2, 2, 4, 2, 3};
  j_MidNormal2 = new Action( "Fh-j-2med", new Animation(times28, 50, 40, 7, "FHouse/FH-j-2med/FH-j-2med"), Cond2,
                               14, 8, 8, Action.AIR, 1, true, true, false, false);
                               j_MidNormal2.ActEffs = new ActEffect[]{new SoundEff("swoosh_2", 0, 0)};
                               j_MidNormal2.jumpCancel = true; j_MidNormal2.dashCancel = true;
                               
  
  int[] times21 = {3, 4, 3, 4, 3, 2, 2};
  Animation Anim21 = new Animation( times21, 50, 50, 7, "FHouse/FH-j.Heavy/FH-j.Heavy");
  j_HeavyNormal = new Action("FH-j-heay", Anim21, Cond3, 9, 5, 9, Action.AIR, 1, true, true, false, false);
  j_HeavyNormal.updFrameDataArr_float(1, -6, -10);
  j_HeavyNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_5", 0, 0), new CounterEff(3, new CounterHit())};
  
  j_DustNormal = new Action("FH-j-t", new Animation( new int[]{3, 3, 3, 2, 2, 5, 3, 3}, 40, 10, 8, "FHouse/FH-j-t/FH-j-t"), new Condition[]{new fPButCond(8)}, 13, 7, 9, Action.AIR, 1, true, true, false, false);
  //j_DustNormal.updFrameDataArr_float(1, -6, -10);
  j_DustNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_5", 0, 0), new OppEff( new SetForce(0, -8, 2), 2, new OppCheck(new InAir()) )};
  
  int[] times24 = {2, 2, 2, 2, 4, 2, 2, 2};
  Condition[] Cond4 = {new comfPButC(new int[][]{{6, 2, 3}, {6, 3, 2, 3, 6}, {6, 3, 2, 3}, {6, 2, 6}}), new fPButCond(5)};
  Special1 = new Action("FH-sp1", new Animation( times24, 50, 8, "FHouse/FH-sp1/FH-1sp"), Cond4, 18, 16, 6, Action.MID, 20, true, true, true, false);
  Special1.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, true), new ForceAddEff(8, 0, false), new ForceAddEff(12, 0, false)};
   
  Condition[] Cond5 = {new facingCond(1), new fPButCond(5)};
   int[] times25 = {4, 3, 6, 2, 3, 6, 3, 3, 3};
   st_fHeavy = new Action("FH-st-forH",//"data/FH-st-fHeavy", 
   new Animation( times25, 75, 9, "FHouse/FH-st.fH/FH-st-fH"), Cond5, 20, 14, 6, Action.HIGH, 2, true, false, false, false);
   
    int[] times21p3 = {4, 4, 4, 4, 4};
    Action Special3p3 = new Action( //"HH-cr-Light",
    new Animation(times21p3, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-3p"), 20, 10, 4, 0, true, true, false, false);
    
    Action FillAct = Special3p3; Special3p3.attWeight = 20;
    FillAct.updFrameDataArr(0, 20);
  FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p2 = {2, 2, 2};
    Action Special3p2 = new Action( "FH-sp3p2",
    new Animation(times21p2, 25, 50, 3,"FHouse/FH-sp3/FH-3sp-2p"), 20, 10, 20, Action.HIGH, true, false, true, false);    
         FillAct = Special3p2; Special3p2.attWeight = 20;
    FillAct.updFrameDataArr(0, 60); FillAct.updFrameDataArr_float(0, 16, 18);
    Special3p2.ActEffs = new ActEffect[]{ new AddOwnForcToOpp(2,1, new Condition()), new ChangeActTo(Special3p3, 1, new Grounded()) };

    
    int[][] revFBvars = {{2, 1, 4}, {2, 1, 4, 7}};
    Condition[] Cond6 = {new InAir(), new comfPButC(revFBvars), new fPButCond(4)};
    int[] times21p1 = {2, 2, 2, 2, 2};
    Special3 = new Action( //"HH-cr-Light",
    new Animation(times21p1, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-1p"), 6, 5, 4, Action.AIR, false, false, false, false); 
    Special3.Conds = Cond6; Special3.attWeight = 20;
    Special3.ActEffs = new ActEffect[]{new ChangeActTo(Special3p2, 1, new ActTimeCond2(0, 9) )};
          FillAct = Special3;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
    Condition[] Cond7 = {new comfPButC(revFBvars), new fPButCond(5)};
     int[] times26 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  Special4 = new Action("FH-sp4", new Animation( times26, 0, 14, "FHouse/FH-sp4/FH-4sp"), Cond7, 32, 10, 10, Action.HIGH, 20, true, true, true, true);
            FillAct = Special4;
    //FillAct.updFrameDataArr(0, 28);
    FillAct.updFrameDataArr_float(1, 16, -22);
    FillAct.updFrameDataArr_float(2, 4, 0);
    FillAct.updFrameDataArr_float(4, 4, 0);
   //FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
   int[][] superm = {{2, 3, 6, 2, 3, 6}, {2, 3, 6, 2, 3}, {2, 6, 2, 6}, {2, 3, 6, 2, 3, 6, 9}};
       Condition[] Cond8 = {new SuperCond(500), new comfPButC(superm), new fPButCond(6)};
     int[] times27= {3, 3, 3, 3, 3,
   3, 3, 3, 3, 3,
 3, 3, 3, 3, 3, 3};
  Super1 = new Action("FH-1super", new Animation( times27, 95, 16, "FHouse/FH-1super/FH-1super"), Cond8, 20, 18, 10, Action.MID, 50, true, false, false, true);
   Super1.superNeed = 500;
   
  LightNormal.addEffs(new GatlingEff(MidNormal2), new GatlingEff(new Condition[]{new ButCond(1)}, cr_LightNormal));
  MidNormal2.addEffs(new GatlingEff(new Condition[]{new ButCond(1)}, cr_HeavyNormal, cr_MidNormal2), new GatlingEff(st_fHeavy, HeavyNormal));
  HeavyNormal.addEffs(new GatlingEff(st_fHeavy));
  cr_LightNormal.addEffs(new GatlingEff(LightNormal, MidNormal2), new GatlingEff(new Condition[]{new ButCond(1)}, cr_MidNormal));
  cr_MidNormal2.addEffs(new GatlingEff(HeavyNormal), new GatlingEff(new Condition[]{new ButCond(1)}, cr_MidNormal, cr_HeavyNormal));
  cr_HeavyNormal.addEffs(new GatlingEff(MidNormal2), new GatlingEff(new Condition[]{new ButCond(1)}, cr_MidNormal2));
  j_LightNormal.addEffs(new GatlingEff(j_MidNormal2));
  j_MidNormal2.addEffs(new GatlingEff(j_LightNormal, j_HeavyNormal, j_DustNormal));
  j_DustNormal.addEffs(new GatlingEff(j_MidNormal2, j_HeavyNormal));
   
    Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, st_fHeavy, FDash, BDash, Jumping, fDiaJump, bDiaJump, Special4, Fireball2, Special1, Super1, NormalThrow},
  {cr_LightNormal, cr_MidNormal2, cr_HeavyNormal, cr_MidNormal, Special1, Super1},
  {j_LightNormal,j_MidNormal2, j_MidNormal, j_HeavyNormal, j_DustNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2, Fireball, Special1, Special3, Special4, AirThrow},
  { Fireball, Fireball2, Special1, Special3, Special4, Super1}
};

  fillActionsList(ActTab);
   
}

void airDash(){
  if(cancelWindow <= 0 || CurAction.dashCancel) ; else return;
  if(curAirActions > 0){
          if(compareBufferWithCombAtt(fDashmotion)){
            CurAction.reset();
          CurAction = airFDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
    if(compareBufferWithCombAtt(bDashmotion)){
            CurAction.reset();
          CurAction = airBDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
  }
}

  void st_Normals(){
        if(CurAction == fWalk){
     // Force.x =  dirMult * sqrt(sq(sin(fWalk.AttAnim.timer* 0.1))) * 6;
    }
  }
  
  void cr_Normals(){
  }
  
  void j_Normals(){
    }   
         
}

//HOCHHAUS###############################################################################################################################################################################################

class F_HHaus extends Fighter{
  Action RoofNormal, BaseNormal, j_MidNormal2;
  Action Special1, Special2, Special4CG, Special5detPlane;
  Animation FB1Anim, FB1DestrEff, FirewallAnim,  FirewallAnimp2;
  ProjAction Fireball, Fireball2, Fireball3;
  Projectile Firewall;
  
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
 
  F_HHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
  }
  
  F_HHaus(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
  }
 
   void copyStdActions(Fighter F){
        Standing = new Action(F.Standing); Crouching = new Action(F.Crouching);
    Knockdown = new Action(F.Knockdown); softKD = new Action(F.softKD); WallStick = new Action(F.WallStick); WallBounce = new Action(F.WallBounce);
    Juggle = new Action(F.Juggle); Stagger = new Action(F.Stagger); BeingGrapped = new Action(F.BeingGrapped);
    InHitStun = new Action(F.InHitStun); HHit = new Animation(F.HHit); LHit = new Animation(F.LHit);
    Blocking = new Action(F.Blocking); HBlock = new Animation(F.HBlock); LBlock = new Animation(F.LBlock);
    Jumping = new Action(F.Jumping); fDiaJump = new Action(F.fDiaJump); bDiaJump = new Action(F.bDiaJump);
    //Jumping2 = new Action(F.Jumping2); fDiaJump2 = new Action(F.fDiaJump2); bDiaJump2 = new Action(F.bDiaJump2); // HHaus has no double jump -> reason for nullptrexcept
    fWalk = new Action(F.fWalk); bWalk = new Action(F.bWalk); //FDash = new Action(F.FDash); 
    BDash = new Action(F.BDash); // maybe adding costume constructor
  }

void specSetup(){
  name = "HochHouse";
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
      
  Falling.AttAnim = new Animation(new int[]{4}, 0, 30, 1, "HHouse/HH-jumpfall/HH-jfall");
      
  int[] times2 = {5, 5, 5, 5, 5, 5, 5};
  Animation Anim1 = new Animation(times2, 30, 7,"HHouse/HH-fWalk/HF-fWalk");
      
  fWalk = new Action(//"HH-HeavyNormal",
  Anim1, 0, 0, 0, 0, true, false, false, false);
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 2.8, 0);
  fWalk.resetAnim = false;
  
  bWalk = new Action(//"HH-HeavyNormal",
  Anim1, 0, 0, 0, 0, true, false, false, false);
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -1.8, 0);
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
  
  /*int[] ani4 = {1, 12, 1, 1};
  FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 10, 0);
    FDash.addAllLists(1, 5, 7, 0);*/
    
  int[] ani5 = {1, 11, 1, 1};
  BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, -5, 0);
  BDash.addAllLists(1, 5, -4, 0);
  
  int[] aniFB2 = {11, 7, 7};
  Condition[] CondFB1 = {new Grounded(), new fPButCond(6), new dirCombCond(new int[]{2,3,6})};
    Fireball2 = new HHaus_ProjAction2(aniFB2, 20, 15, 2, 0, false, false, true, false, 1, -1, 4.0, 0, false, false, false);
    Fireball2.Conds = CondFB1; Fireball2.attWeight = 18;
  Fireball2.updFrameDataArr(0, 25);
  Fireball2.updFrameDataArr_float(0, 0, 0);
  Fireball2.addAllLists(1, 40, 0, 0);
  
  int[] aniFB3 = {11, 3, 3};
    Condition[] CondFB2 = {new Grounded(), new fPButCond(5), new dirCombCond(new int[]{2,1,4})};
    Fireball3 = new HHaus_ProjAction3(aniFB3, 0, 0, 0, 0, false, false, false, false, 1.2, 200, 2.0, 0, true, true, false);
    Fireball3.Conds = CondFB2; Fireball3.attWeight = 18;
  Fireball3.updFrameDataArr(0, 25);
  Fireball3.updFrameDataArr_float(0, 0, 0);
  Fireball3.addAllLists(1, 40, 0, 0);
  
    int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Crouching = new Action(//"bn-cr", 
  new Animation(times1, 0, 7, "HHouse/HH-cr.idle/HH-cr.idl"), false);
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
  
   /* int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"HHouse/HH-sKD/HH-air-sKD");
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"HHouse/HH-hKD/HH-hKD");*/
  
    int[] ani13 = {11};
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 160, 300, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
  int[] ani12 = {0, 9, 9};
  RoofNormal = new Action(ani12,  5, 2, 30, Action.HIGH, true, true, false, false);
  RoofNormal.updFrameDataArr(0, 15); 
  RoofNormal.updFrameDataArr_float(0, 0, 0);
  RoofNormal.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  RoofNormal.addAllLists(1, 4, 3, 0);
  RoofNormal.HitBoxCollect.get(1).add(new ColCircle( 150, -125, 160, 300, 0, 0, -1));
  RoofNormal.HurtBoxCollect.get(1).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  RoofNormal.addAllLists(2, 20, 0, 0);
  RoofNormal.HurtBoxCollect.get(2).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  
     int[] times15 = {2, 3, 1, 1, 2, 4};
     Condition[] Cond0 = {new fPButCond(4)};
  LightNormal = new Action("HH-st-light", new Animation(times15, 50, 6,"HHouse/HH-st.Light/HH-st.Light"), Cond0, 10, 5, 2, 0, 1, false, false, false, false);
  
   int[] times16 = {1, 1, 1, 1, 2, 2, 2, 3, 1, 1, 1, 1, 1, 1};
   Condition[] Cond1 = {new fPButCond(6)};
  MidNormal2 = new Action("HH-st-mid", new Animation(times16, 100, 14,"HHouse/HH-st.Mid/HH-st.Mid"), Cond1, 18, 16, 8, 0, 6, false, false, false, false);

  int[] times17 = {6, 6, 2, 2, 3, 2, 4, 7, 3, 3}; //35
  Condition[] Cond2 = {new fPButCond(5)};
  HeavyNormal = new Action("HH-HeavyNormal",
  new Animation(times17, 150, 10,"HHouse/HH-st.Heavy/HH-st.Heavy"), Cond2, 16, 10, 16, 0, 7, false, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.STAGGER, new CounterHit())};
  
    int[] times18 = {4, 5, 3, 2};
    cr_LightNormal = new Action( "HH-cr-Light",
    new Animation(times18, 25, 4,"HHouse/HH-cr.Light/HH-cr.Light"), Cond0, 12, 6, 4, 0, 2, false, false, false, false);
    
        int[] times19 = {3, 4, 4, 2, 4, 3, 2};
    cr_MidNormal = new Action( "HH-cr-med",
    new Animation(times19, 75, 7,"HHouse/HH-cr.med/HH-cr.med"), new Condition[]{new ButCond(1), new fPButCond(8)}, 
    20, 8, 12, Action.LOW, 7, false, false, true, false);
    
    cr_MidNormal2 = new Action( "HH-cr-m2", new Animation(new int[]{4, 4, 2, 4, 4}, 25, 5,"HHouse/HH-cr-m2/HH-cr-2m"),
    Cond1, 18, 8, 8, Action.MID, 6, true, false, false, false);
    
        int[] times20 = {5, 5, 2, 2, 5, 3, 3};
    cr_HeavyNormal = new Action( "HH-cr-Heay",
    new Animation(times20, 25, 7,"HHouse/HH-cr.Heavy/HH-cr.Heavy"), Cond2, 20, 15, 30, 0, 7, false, false, false, false);
    cr_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.JUGGLE, new CounterHit()) };
    cr_HeavyNormal.jumpCancel = true;
    
    
    int[] times21p3 = {2, 2, 2, 2};
    Action j_MidNormalp3 = new Action( //"HH-cr-Light",
    new Animation(times21p3, 30, 4,"HHouse/HH-j.med/p3/HF-j.med"), 6, 5, 4, 0, true, true, false, false);
    
    Action FillAct = j_MidNormalp3;
    FillAct.updFrameDataArr(0, 10);
  FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p2 = {2, 2};
    Action j_MidNormalp2 = new Action( "HH-j-medp2",
    new Animation(times21p2, 30, 2,"HHouse/HH-j.med/p2/HF-j.med"), 6, 5, 4, 0, false, false, false, true);
      j_MidNormalp2.ActEffs = new ActEffect[]{ 
      new SetForce(10, 0, 1, new facingCond(1)), new SetForce(-10, 0, 1, new facingCond(-1)), 
      new ChangeActTo(j_MidNormalp3, 1, new FalseCond(new ButCond(6)) ),
      new ChangeActTo(j_MidNormalp3, 1, new ActTimeCond2(0, 20))};
    
    int[] times21p1 = {2, 2, 2, 2, 2};
    j_MidNormal = new Action( //"HH-cr-Light",
    new Animation(times21p1, 30, 5,"HHouse/HH-j.med/p1/HF-j.med"), 6, 5, 4, 0, false, false, false, false); 
    j_MidNormal.Conds = Cond1; j_MidNormal.attWeight = 3;
          FillAct = j_MidNormal;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   j_MidNormal.ActEffs = new ActEffect[]{ new ChangeActTo(j_MidNormalp2, 1, new ActTimeCond2(0, 10) ) };
   
   
   Condition[] Cond3 = {new ButCond(1), new fPButCond(6)};
   int[] times29 = {4, 3, 6, 3, 3};
   j_MidNormal2 = new Action( "HH-j-2med",
    new Animation(times29, 0, 5,"HHouse/HH-j-2med/HH-j-2med"),Cond3 , 12, 8, 10, Action.AIR, 5, false, false, false, false); 
    j_MidNormal2.updFrameDataArr_float(1, 0, 16);
   
      int[] times22 = {3, 2, 4, 2};
    j_LightNormal = new Action( "HH-j-light",
    new Animation(times22, 30, 4,"HHouse/HH-j.light/FH-j.light"), Cond0, 8, 4, 4, Action.AIR, 1, true, true, false, false); 
    j_LightNormal.jumpCancel = true;
    
          int[] times23 = {2, 2, 2, 2, 4, 2, 4, 2, 2, 2, 2};
    j_HeavyNormal = new Action( "HH-j-heay",
    new Animation(times23, 50, 30, 11,"HHouse/HH-j.Heavy/FH-j.Heavy"), Cond2, 10, 7, 20, Action.HIGH, 6, true, true, true, false); 
    j_HeavyNormal.gravMult = 0.93; j_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.WALLSTICK, new Condition[]{new CounterHit()}), new SoundEff("hf-j-h-pumpit", 0, 0)}; //GEFAHR
   
   int[] backCharge = {6}; 
   Condition[] Cond4 = {new ChargeDirCheck(20, 1), new fPButCond(5), new dirCombCond(backCharge),
  };      
  
  int[] revFB = {2, 1, 4}, FB = {2, 3, 6};
  Condition[] CondFB0 = {new Grounded(), new fPButCond(4), new dirCombCond(FB)};
  int[] times7 = {3, 3, 3, 3, 4, 4, 4, 3, 2, 3, 3};
  Fireball = new HHaus_ProjAction1( new Animation(times7, 0, 11, "HHouse/HH-sp2/HH-2sp"),
  18, 30, 10, 0, false, false, false, false, 1, 300, -6.0, 0, false, false, true);
  Fireball.Conds = CondFB0; Fireball.attWeight = 18;
  Fireball.ProjAnim = FB1Anim;
  Fireball.destrEffAnim = FB1DestrEff;
  Fireball.updFrameDataArr(0, 15);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  Fireball.addAllLists(1, 20, 0, 0);
  Fireball.HurtBoxCollect.get(1).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  
            int[] times24 = {3, 3, 2, 6, 2, 2, 4, 4, 2, 2};
    Special1 = new Action( "HH-sp1",
    new Animation(times24, 30, 10,"HHouse/HH-sp1/HH-1sp"),Cond4, 15, 7, 12, Action.MID, 20, true, true, false, false); 
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
  
     Condition[] Cond5 = {new Grounded(), new fPButCond(4), new dirCombCond(revFB)};
      int[] times26 = {2, 2, 2, 3, 6, 6, 6, 3, 3};
  Special2 = new ChangeAction("HH-sp3-p1", new Animation(times26, 0, 9,"HHouse/HH-sp3/HH-3sp-1p"),
  5, 2, 5, -1, true, true, false, false, NormalGrab);
  Special2.Conds = Cond5; Special2.attWeight = 20;
 /* Special2 .updFrameDataArr(0, 6); 
  
    Special2 .HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  Special2 .addAllLists(1, 12, 3, 0);
  
  Special2 .HurtBoxCollect.get(1).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  Special2 .addAllLists(2, 10, 0, 0);
  Special2 .HurtBoxCollect.get(2).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));*/
    Special2 .HitBoxCollect.get(1).add(new ColCircle( 10, -270, 280, 200, 0, 0, -1));
    Special2 .updFrameDataArr_float(0, 10, -20);
    
      float[][] xyGrab1 = {{200, -80},
                      {200, -80},
                      {210, -84}, 
                    {230, -88}};
  Action Special4CGp2 = new Action("HH-sp4cgp2", 
  new Animation( new int[]{6, 4, 2, 4, 4,    4, 4, 4, 4, 6,   4, 4, 4, 4, 4} , 150, 14, "HHouse/HH-sp4CG/p2/HH-4sp"), 
  new Condition[]{new Condition()}, 
  2, 0, 0, Action.GRAB, 1, true, false, false, false);
 Special4CGp2.ActEffs = new ActEffect[]{new GrabEff(xyGrab1 , new int[]{16,6,6,6}), new PutInHitSt(30, 34, new ActTimeCond2(0,36) ), new OppEff(new SetForce(-26, -8, 1), 1, new ActTimeCond2(0,36) )};
 Special4CGp2.hitCancel = false;

  Special4CG = new Action("HH-sp4cg", 
  new Animation( new int[]{2, 4, 2, 4, 2,   4, 2, 4, 2} , 80, 8, "HHouse/HH-sp4CG/HH-4sp"), 
 new Condition[]{new fPButCond(5), new comfPButC(new int[][]{ {2,3,6} } )}, 
  10, 0, 0, Action.GRAB, 30, true, false, false, false);
 Special4CG.ActEffs = new ActEffect[]{new ChangeActTo(Special4CGp2, 0, new Condition())};
 Special4CG.hitCancel = false;
 
  /*  OilProj = new WH_Oilpuddle(0.0, 0.0, 200, 0, 200, 100, 0.0, 0.0, 1, 600, 0, 0, 0, false, false, false, false, false) ;
   OilProj.setAnims(new Animation(new int[]{10}, 0, -25, 1, "Projectiles/Oilpuddle/Oilpuddle"), new Animation(new int[]{10}, 0, -25, 1, "Projectiles/Oilpuddle/Oilpuddle"));
                  int[] times32 = {2, 6, 2, 6, 2,
                2, 2, 6, 2, 2,
              2, 3, 2};
         Condition[] Cond8 = {new fPButCond(5), new dirCombCond(revFB)};
   Special5 = new Action("Wh-sp2",
   new Animation( times32, 100, 10, "WHouse/WH-sp5-oil/WH-5sp-oil"), Cond8, 32, 10, 4, Action.MID, 18, true, false, false, false);
   Special5.ActEffs = new ActEffect[]{ new ProjAddEff( OilProj, 1, new ActTimeCond(0, 1) )};*/
 
 FirewallAnim = new Animation(new int[]{4, 4, 4, 4,   4, 4, 4, 4}, 0, 0, 8, "Projectiles/PlaneFirewall/PlaneFW"); 
 FirewallAnimp2 = new Animation(new int[]{4, 4, 4, 4}, 0, -420, 4, "Projectiles/PlaneFirewall/p2/PlaneFW");
 Firewall = new Projectile(0.0, 0.0, 0, 0, 70, 860, -3.0, 0.0, 1, 32, 50, 20, 8, false, false, false, true, true);
 Firewall.setAnims(FirewallAnim, FirewallAnimp2);
 
  Special5detPlane = new Action("HH-sp5", 
  new Animation(new int[]{2, 4, 8, 2, 8, 2, 2, 8, 2, 2, 2, 2 }, 50, 12,"HHouse/HH-sp5/HH-5sp"), 
  new Condition[]{new fPButCond(6), new comfPButC(new int[][]{ {2,1,4} } )}, 
  18, 16, 8, 0, 32, true, false, false, false);
  //Special5detPlane.ActEffs = new ActEffect[]{ new ProjAddEff( Firewall, 1, new ActTimeCond(0, 1) )};
  
  Action Special6trip = new Action("HH-sp6", 
  new Animation(new int[]{4, 4, 2, 4, 2, 4, 8, 4, 3, 3 }, 140, 10,"HHouse/HH-sp6/HH-6sp"), 
  new Condition[]{new Grounded(), new fPButCond(6), new comfPButC(new int[][]{ {2,3,6} })},//{4,1,2,3,6}, {4,2,6}, {4,1,2,3,6} } )}, 
  36, 20, 14, Action.HIGH, 32, true, false, false, false);
  Special6trip.ActEffs = new ActEffect[]{new CounterEff(Action.GROUNDBOUNCE, new CounterHit())};
    
        Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, Special2, Fireball,//Fireball2, Fireball3, 
Special1, Special5detPlane, Special4CG, Special5detPlane, Special6trip, NormalThrow},
  {cr_LightNormal, cr_MidNormal2, cr_MidNormal, cr_HeavyNormal},
  {j_LightNormal, j_MidNormal, j_MidNormal2, j_HeavyNormal, AirThrow},
  {Special1, Fireball, Special6trip}
};
  fillActionsList(ActTab);
}

void dash(){
      if(compareBufferWithCombAtt(bDashmotion) && (y >= GROUNDHEIGHT || CollisionBox.bottom)
      ){
          CurAction.reset();
          CurAction = BDash;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }
}

void airDash(){}

void st_Normals(){  
}
void cr_Normals(){
}
void j_Normals(){
}

void extraStuff(){
  if(CurAction == Special5detPlane && CurAction.curCollumn == 0 && CurAction.curMoveDur == 1){
    for(int i = Projectiles.size()-1; i >= 0; i--){
      Projectile p = Projectiles.get(i), 
      p2 = Firewall.copy(); p2.setXY(dirMult, p.x, p.y);
      p2.setAnims(FirewallAnim, FirewallAnimp2);
      p.exTimer = 0;
      Projectiles.set(i, p2);
      
    }
  }
}



}


//Wohnwagen###############################################################################################################################################################################################
class F_WHaus extends Fighter{
  //TODO: For Higher gear levels: reverse weight normal cancel; more special properties on moves; making airdash better; maybe invu on DP 
  
  boolean stance = false;
  int gearLevel = 0, gearTime = 0;
  
  WH_Oilpuddle OilProj; Projectile SpeedProj;
  Action fDash_gl0, fDash_gl3, bDash_gl0, bDash_gl3, Stance, stanceFWalk, stanceBWalk, StanceFDash;
  Action StanceLight, StanceMed, StanceHeavy;

  Action Special1, Special2, Special3rek1, Special3rek2, Special4, Special5, 
  Special6drill, Sp7speedProj, Sp8speedDP, Super1RTL;
  
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
  
  F_WHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
  }
  
   F_WHaus(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
  }
  
void specSetup(){
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
      
  Falling.AttAnim = new Animation(new int[]{4}, 0, 1, "WHouse/WH-jumpfall/WH-jfall");
  Landing.AttAnim = new Animation(new int[]{3}, 0, 1, "WHouse/WH-landing/WH-landing");
  Turning.AttAnim = new Animation(new int[]{3,3,3},  0, 3, "WHouse/WH-turning/WH-turning");
  
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times0, 0, 1, "WHouse/WH-fWalk/WH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 4.2, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -3, 0);
  
  Animation Anim1_1 = new Animation(times0, 0, 1, "WHouse/WH-stance/WH-stance");
      Stance = new Action( Anim1_1, false);
      Stance.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      Stance.updFrameDataArr(0, 1);
     Stance.updFrameDataArr_float(0, 0, 0);

  Animation Anim2_1 = new Animation(times0, 0, 1, "WHouse/WH-stance-fWalk/WH-stance-fW");
  stanceFWalk = new Action(Anim2_1, false);//0, 0, 0, 0, true, false, false, false);
  stanceFWalk.addingForce = true;
  stanceFWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  stanceFWalk.updFrameDataArr(0, 1);
  stanceFWalk.updFrameDataArr_float(0, 1.6, 0);//3, 0);
  
  Animation Anim2_2 = new Animation(times0, 0, 1, "WHouse/WH-stance-bWalk/WH-stance-bW");
  stanceBWalk = new Action(Anim2_2, false);
  stanceBWalk.addingForce = true;
  stanceBWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  stanceBWalk.updFrameDataArr(0, 1);
  stanceBWalk.updFrameDataArr_float(0, -1.2, 0);
  
  int[] times3 = {2, 3};
  Animation Anim3 = new Animation(times3, 0, 2, "WHouse/WH-nJump/WH-nJump");
  Anim3.loop = false;
  Jumping = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.addAllLists(1, 2, 0, -18);
  
  fDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.addAllLists(1, 2, 10, -15);
  
  bDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.addAllLists(1, 2, -7.5, -15);
  
    Jumping2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
 int[] ani4 = {8, 2, 4, 4, 2};
  fDash_gl0 = new Action("WH-fdash", new Animation(ani4, 0, 5, "WHouse/WH-dash/WH-fdash"),
  0, 0, 0, 0, false, false, false, false);
  fDash_gl0.ActEffs = new ActEffect[]{new ArmorEff(0, 1, 1)};
  
  airFDash = new Action("WH-aifdash", new Animation(ani4, 0, 5, "WHouse/WH-dash/WH-fdash"),
  0, 0, 0, 0, false, false, false, false);
  airFDash.allButSelfCancel = true;
    
  fDash_gl3 = new Action( "WH-tpfdash", new Animation(new int[]{4, 2, 2, 2, 2, 4, 2, 2, 4}, 70, 9, "WHouse/WH-tpdash/WH-tpdash"),
  5, 0, 0, 0, false, false, false, false);
  fDash_gl3.collision = false; fDash_gl3.pushBackMult = 0;
  fDash_gl3.ActEffs = new ActEffect[]{new SetForce(35, 0, 1, new facingCond(1), new ActTimeCond3(1, 2) )};
    
  FDash = fDash_gl0;
    
  int[] ani5 = {8, 2, 12, 2};
  bDash_gl0 = new Action("WH-bdash", new Animation(ani5, 0, 4, "WHouse/WH-dash/WH-bdash"), 0, 0, 0, 0, false, false, false, false);
  
  airBDash = new Action("WH-aibdash", new Animation(ani5, 0, 4, "WHouse/WH-dash/WH-bdash"), 0, 0, 0, 0, false, false, false, false);
  
    bDash_gl3 = new Action( "WH-tpbdash", new Animation(new int[]{4, 2, 2, 2, 2, 4, 2, 2, 4}, 70, 9, "WHouse/WH-tpdash/WH-tpdash"),
  5, 0, 0, 0, false, false, false, false);
  bDash_gl3.collision = false; bDash_gl3.pushBackMult = 0;
  bDash_gl3.ActEffs = new ActEffect[]{new SetForce(-35, 0, 1, new facingCond(-1), new ActTimeCond3(1, 2) )};
  
  BDash = bDash_gl0;
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action("wh-cr", new Animation(times2, 0, 1, "WHouse/WH-cr/WH-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "WHouse/WH-Stun/WH-HHitSt"); LHit = new Animation(times1, 0, 1, "WHouse/WH-Stun/WH-LHitSt");
  HBlock = new Animation(times1, 0, 1, "WHouse/WH-Stun/WH-HBlockSt"); LBlock = new Animation(times1, 0, 1, "WHouse/WH-Stun/WH-LBlockSt");
  
  int[] ani9 = {6};
  Blocking = new Action(ani9);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 200, 160));
  
  int[] ani10 = {2};
  InHitStun = new Action(ani10, 0, 0, 0, 0, true, true, false, false);
  InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160));
  
  /*softKD.AttAnim = new Animation(times0_0, 0, 1,"WHouse/WH-sKD/WH-air-sKD");  
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"WHouse/WH-hKD/WH-hKD");*/
  
  int[] ani13 = {11};
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 200, 160, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
    int[] times14 = {4, 2, 4, 2, 2};
    Condition[] Cond0 = {new fPButCond(4)};
  Animation Anim14 = new Animation(times14, 0, 5, "WHouse/WH-st-light/WH-st-light");
  LightNormal = new Action("WH-st-light", Anim14, Cond0, 15, 7, 5, 3, 1, true, true, false, false);
  
    int[] times16 = {2, 4, 4, 3, 2, 3, 5, 2, 2, 1, 1};
    Condition[] Cond1 = {new fPButCond(6)};
    Animation Anim16 = new Animation(times16, 70, 11,"WHouse/WH-st-med/WH-st-med");
  MidNormal2 = new Action("WH-st-med", Anim16, Cond1, 20, 12, 15, 3, 4, true, true, false, false);
  MidNormal2.ActEffs = new ActEffect[]{new SoundEff("swoosh_2", 0, 1), new SoundEff("metalclank_0", 1, 2), new CounterEff(Action.STAGGER, new CounterHit())};
  
    int[] times17 = {4, 4, 4, 2, 6, 6, 2, 2, 2};
    Condition[] Cond2 = {new fPButCond(5)};
    Animation Anim17 = new Animation(times17, 70, 9,"WHouse/WH-st-heay/WH-st-heay");
  HeavyNormal = new Action("WH-st-heay", Anim17, Cond2, 22, 16, 20, Action.MID, 7, true, true, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_0", 0, 1), new SoundEff("bite_0", 2, 1)};
  
  int[] times18 = {5, 8, 2};
  Animation Anim18 = new Animation(times18, 0, 3, "WHouse/WH-cr-light/WH-cr-light");
  cr_LightNormal = new Action("WH-cr-light", Anim18, Cond0, 14, 7, 4, 3, 2, true, false, false, false);

  int[] times19 = {4, 4, 2, 2, 4, 4, 2, 2, 2, 4, 4};
  Animation Anim19 = new Animation(times19, 70, 11, "WHouse/WH-cr-med/WH-cr-med");
  cr_MidNormal = new Action("Wh-cr-med", Anim19, new Condition[]{new fPButCond(8)}, 40, 10, 4, Action.LOW, 8, true, true, true, false);
  
   cr_MidNormal2 = new Action( "Wh-cr-m2", new Animation(new int[]{3, 3, 3, 2, 6, 4, 4}, 0, 7,"WHouse/WH-cr-m2/WH-cr-2m"),
    Cond1, 18, 9, 8, Action.MID, 6, true, false, false, false);
  
    int[] times23 = {6, 3, 3, 6, 4, 4, 2, 6};
  cr_HeavyNormal = new Action( "Wh-cr-heay", new Animation(times23, 0, 8, "WHouse/WH-cr-heay/WH-cr-heay"), Cond2,
                               24, 14, 18, Action.MID, 8, true, false, true, false);
                               cr_HeavyNormal.jumpCancel = true;
  cr_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.JUGGLE, new CounterHit()), 
  new CounterEff(Action.JUGGLE, new WH_gearCheck(2, this))};
  
  int[] times20 = {3, 2, 4, 3};
  Animation Anim20 = new Animation(times20, 30, 0, 4, "WHouse/WH-j-light/WH-j-light");
  j_LightNormal = new Action("Wh-j-light", Anim20, Cond0, 14, 4, 3, Action.AIR, 1, true, true, false, false);
  j_LightNormal.jumpCancel = true; j_LightNormal.dashCancel = true;
  
  int[] times22 = {2, 2, 3, 2, 4, 2, 2, 3};
  j_MidNormal = new Action( "Wh-j-med", 
  new Animation(times22, 90, 8, "WHouse/WH-j-med/WH-j-med"), Cond1, 18, 8, 10, Action.AIR, 3, true, true, false, false);
  j_MidNormal.jumpCancel = true; j_MidNormal.dashCancel = true;
  
    int[] times21 = {3, 3, 2, 3, 2, 3, 2, 3, 2, 2};
  Animation Anim21 = new Animation( times21, 50, 50, 10, "WHouse/WH-j-h/WH-j-h");
  j_HeavyNormal = new Action("WH-j-h", Anim21, Cond2, 14, 7, 18, Action.AIR, 4, true, true, false, false);
  j_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.WALLBOUNCE, new WH_gearCheck(2, this)),
  new CounterEff(Action.WALLBOUNCE, new CounterHit())};
  
  Action j_DustNormalp2 = new Action("Wh-j-tp2", new Animation( new int[]{2, 6, 2, 2, 2, 2}, 50, 6, "WHouse/WH-j-t/p2/WH-j-t"), 
  new Condition[]{}, 0, 0, 0, Action.MID, 7, true, false, false, false);
  
    j_DustNormal = new Action("Wh-j-t", new Animation( new int[]{3, 3, 3, 2, 40}, 50, 5, "WHouse/WH-j-t/WH-j-t"), 
  new Condition[]{new fPButCond(8)}, 20, 10, 9, Action.HIGH, 7, true, false, false, false);
  j_DustNormal.ActEffs = new ActEffect[]{ new GravEff(0, 0, true), new GravEff(2, 0, false), new AddOwnForcToOpp(2, 1), new ChangeActTo(j_DustNormalp2, 1, new Grounded() ) };
  
  int[] times24 = {3, 3, 3, 4, 3, 4, 2, 2, 2, 4, 4, 2};
  Animation Anim24 = new Animation( times24, 50, 0, 12, "WHouse/WH-j-heay/WH-j-heay");
  Special6drill = new Action("Wh-j-heay", Anim24, new Condition[]{new WH_gearCheck(3, this), new InAir(), new fPButCond(4), new comfPButC(new int[][]{ {2, 3,  6} }) },
  16, 10, 7, Action.AIR, 20, false, true, false, true);
  Special6drill.gravMult = 0.70;
  //Special6drill.ActEffs = new ActEffect[]{ new AddOwnForcToOpp( 2, 1, new Condition() )};
           Action FillAct = Special6drill;
   FillAct.updFrameDataArr_float(0, 22, 0);
   
   /*int[] times25 = {4, 3, 6, 2, 3, 6, 3, 3, 3};
   st_fHeavy = new Action("FH-st-forH",//"data/FH-st-fHeavy", 
   new Animation( times25, 75, 9, "FHouse/FH-st.fH/FH-st-fH"), 18, 14, 20, Action.HIGH, true, false, false, false);*/
   
         int[] times26 = {3, 3, 3, 2, 2, 3, 6, 4, 2, 2, 3, 2, 2};
      Condition[] Cond3 = {new Grounded(), new fPButCond(5), new VertForceCheck(5.0)};
   Special1 = new Action(//"FH-st-forH",
   new Animation( times26, 0, 13, "WHouse/WH-sp1/WH-1sp"), 18, 6, 10, Action.HIGH, true, true, true, false);
   Special1.Conds = Cond3; Special1.attWeight = 20; 
   Special1.ActEffs = new ActEffect[]{new ChangeAnimCoords(0, 0, new ActTimeCond2(0, 1) ), new ChangeAnimCoords(0, 200, new ActTimeCond2(3, 1) ), new WH_stance(this, 1, new ActTimeCond2(5, 2) ), 
   new ForceAddEff(0, 0, true), new ForceAddEff(1, 4, false)
 };
   Special1.hitCancel = false;
   FillAct = Special1;
   FillAct.updFrameDataArr(0, 6);
   FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   FillAct.addAllLists(1, 4, 6, 0);
   FillAct.addAllLists(2, 1, 24, -42);

   FillAct.addAllLists(3, 15, 10, -20);
   FillAct.HurtBoxCollect.get(3).add(new ColCircle(0, 0, 330, 100));
      FillAct.HitBoxCollect.get(3).add(new ColCircle(30, 0, 360, 120));
   FillAct.addAllLists(4, 7, 0, 0);
      FillAct.HurtBoxCollect.get(4).add(new ColCircle(0, 0, 330, 100));
   //FillAct.HitBoxCollect.get(1).add(new ColCircle(30, 0, 360, 120));
   FillAct.addAllLists(5, 4, 0, 0);
   
   int[] revFB = {2, 1, 4}, FB = {2, 3, 6};
         int[] times28 = {3, 3, 2, 2, 2, 4, 2, 2, 2, 2};
         Condition[] Cond4 = {new fPButCond(6), new dirCombCond(revFB) /*new VertForceCheck(-4.0)*/};
   Special2 = new Action("Wh-sp2",
   new Animation( times28, 100, 10, "WHouse/WH-sp2/WH-2sp"), Cond4, 32, 10, 4, Action.MID, 18, true, true, false, false);
   Special2.hitCancel = false; Special2.ActEffs = new ActEffect[]{new CounterEff(Action.STAGGER)};
   
         int[] times29 = {4, 4, 3, 8, 4, 4, 2, 3};
         Condition[] Cond5 = {new WH_checkStance(this, false), new WH_gearCheck(1, this), new Grounded(), new fPButCond(4), new dirCombCond(FB)};
   Special3rek1 = new Action("Wh-sp3-rek1",
   new Animation( times29, 65, 8, "WHouse/WH-sp3-rekka1/WH-4sp-1rekka"), Cond5, 22, 10, 14, Action.MID, 18, true, false, false, false);
   Special3rek1.dashCancel = true;
   
            int[] times30 = {5, 4, 3, 8, 6, 4, 3, 2};
         Condition[] Cond6 = {new WH_checkStance(this, false), new WH_gearCheck(1, this), new Grounded(),
       new CheckStateCond(Special3rek1), new CheckAttPart(2), new fPButCond(4), new comfPButC(new int[][]{FB, {2, 6}, {3, 6} } )};
   Special3rek2 = new Action("Wh-sp3-rek2",
   new Animation( times30, 65, 8, "WHouse/WH-sp3-rekka2/WH-4sp-2rekka"), Cond6, 22, 10, 14, Action.MID, 26, true, false, false, false);
   Special3rek2.ActEffs = new ActEffect[]{new CounterEff(2, new Condition()), 
   new ChangeActTo(Special1, 1, new CheckAttPart(2), new fPButCond(5), new comfPButC(new int[][]{FB, {2, 6}, {3, 6} } ), new WH_gearCheck(2, this) ) };
   Special3rek2.jumpCancel = true; Special3rek2.dashCancel = true;
   
               int[] times31 = {4, 3, 2, 4, 6, 4, 2, 3};
         Condition[] Cond7 = {new WH_checkStance(this, false), new Grounded(), new WH_gearCheck(2, this), new fPButCond(6), new dirCombCond(FB), new OrCond( 
      new Condition[]{},
      new Condition[]{new CheckStateCond(Special3rek1), new CheckAttPart(2)}
   )  
   };
   Special4 = new Action("Wh-sp4-slide",
   new Animation( times31, 65, 8, "WHouse/WH-sp4-slide/WH-4sp-slide"), Cond7, 24, 10, 4, Action.LOW, 26, true, true, true, false);
   
   OilProj = new WH_Oilpuddle(0.0, 0.0, 200, 0, 200, 100, 0.0, 0.0, 1, 600, 0, 0, 0, false, false, false, false, false) ;
   OilProj.setAnims(new Animation(new int[]{10}, 0, -25, 1, "Projectiles/Oilpuddle/Oilpuddle"), new Animation(new int[]{10}, 0, -25, 1, "Projectiles/Oilpuddle/Oilpuddle"));
                  int[] times32 = {2, 6, 2, 6, 2,
                2, 2, 6, 2, 2,
              2, 3, 2};
         Condition[] Cond8 = {new fPButCond(5), new dirCombCond(revFB)};
   Special5 = new Action("Wh-sp2",
   new Animation( times32, 100, 10, "WHouse/WH-sp5-oil/WH-5sp-oil"), Cond8, 32, 10, 4, Action.MID, 18, true, false, false, false);
   Special5.ActEffs = new ActEffect[]{ new ProjAddEff( OilProj, 1, new ActTimeCond2(3, 2) )};
   
   SpeedProj = new Projectile(0,0, 200, -100, 70, 100, 10, 0, 0.0, 80, 30, 20, 8, false, false, false, true, true) ;
   SpeedProj.setAnims(new Animation(new int[]{3, 4, 6, 6, 6}, 0, -25, 5, "Projectiles/speedproj/speedproj"), new Animation(new int[]{3,3,3,3}, 0, -25, 4, "Projectiles/speedproj/p3/speedproj"));

   Sp7speedProj = new Action("Wh-sp7",
   new Animation( new int[]{3, 2, 3, 3, 10,  4, 3, 3, 10, 4,  4, 4}, 115, 12, "WHouse/WH-sp-speedproj/WH-sp-speedproj"),
   new Condition[]{new WH_gearCheck(1, this), new fPButCond(5), new dirCombCond(FB)},
   10, 10, 4, Action.MID, 18, true, false, false, false);
   Sp7speedProj.ActEffs = new ActEffect[]{ new ProjAddEff( SpeedProj, 1, new FirstHitCheck(), new ActTimeCond2(4, 1) ), 
   new WH_gearAdd(-1, this, 1, new ActTimeCond2(0, 2) )};
   
   Sp8speedDP = new Action("Wh-sp8",
   new Animation( new int[]{2, 3, 5, 3, 2, 5, 5, 3, 3, 4, 3}, 25, 11, "WHouse/WH-sp-speedDP/speedDP"),
   new Condition[]{new Grounded(), new WH_gearCheck(1, this), new fPButCond(5), new comfPButC( new int[][]{{6, 2, 3}, {6, 3, 2, 3, 6}, {6, 3, 2, 3}, {6, 2, 6}})},
   20, 10, 12, Action.MID, 18, true, false, false, false);
   Sp8speedDP.ActEffs = new ActEffect[]{new FirstHitEff(1, 0, true), new FirstHitEff(2, 1, true), new FirstHitEff(3, 1, true),// new ProjAddEff( SpeedProj, 1, new FirstHitCheck(), new ActTimeCond2(4, 1) ), 
   new WH_gearAdd(-1, this, 1, new ActTimeCond2(0, 2) )};
   
                     int[] times33 = {2, 4, 4, 4, 4, 2, 4, 4, 4, 4, 2, 4, 4};
   Super1RTL = new Action("Wh-1super", new Animation( times33,0, 100, 13, "WHouse/WH-1superRTL/WH-1superRTL"), 32, 18, 4, Action.MID, false, false, false, true);
      int[][] superm = {{2, 3, 6, 2, 3, 6}, {2, 3, 6, 2, 3}, {2, 6, 2, 6}, {2, 3, 6, 2, 3, 6, 9}};
       Super1RTL.Conds = new Condition[]{new SuperCond(500), new comfPButC(superm ), new fPButCond(5)};
       Super1RTL.ActEffs = new ActEffect[]{ new AddOwnForcToOpp(2, 1) }; 
       Super1RTL.superNeed = 500; Super1RTL.attWeight = 40;
       
  StanceFDash = new Action("WH-stance-fdash", 
  new Animation(new int[]{3, 3, 3, 2, 2, 5, 2, 3, 2}, 0, 0, 9, "WHouse/WH-stance-fdash/WH-stance-fdash"),
  new Condition[]{new Grounded(), new comfPButC(new int[][]{{6, 6}} )}, 6, 4, 5, Action.MID, 10, true, false, false, false);
  StanceFDash.collision = false;    

    int[] etimes1 = {2, 4, 3, 3};
  StanceLight = new Action("WH-stance-l", 
  new Animation(etimes1, 0, 4, "WHouse/WH-stance-light/WH-stance-l"), new Condition[]{new Grounded(), new fPButCond(4)}, 6, 4, 5, Action.LOW, 10, true, false, false, false);
  StanceLight.hitCancel = false;
  
    int[] etimes2 = {3, 3, 3};
  StanceMed = new DirChangeAct("WH-stance-m", 
  new Animation(etimes2, 0, 3,"WHouse/WH-stance-Med/WH-stance-m"), new Condition[]{new Grounded(), new fPButCond(6)}, 4, 1, 10, Action.MID, 11, true, true, false, false);
  StanceMed.hitCancel = false;
  
    int[] etimes3 = {3, 3, 3, 3, 6};
  StanceHeavy = new Action("WH-stance-heay", 
  new Animation(etimes3, 0, 5,"WHouse/WH-stance-heavy/WH-stance-h"), new Condition[]{new Grounded(), new fPButCond(5)}, 14, 12, 20, Action.HIGH, 12, true, true, false, true);
  StanceHeavy.hitCancel = false;
  
        Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, Special1, Special2, Special3rek1, Special3rek2, Special4, Special5, Sp8speedDP, Sp7speedProj, Super1RTL, NormalThrow},
  {cr_LightNormal, cr_MidNormal2, cr_HeavyNormal, cr_MidNormal, Super1RTL},
  {j_LightNormal, j_MidNormal, j_HeavyNormal, j_DustNormal, AirThrow, Special6drill, Super1RTL},
  {Special3rek2, Special3rek1, Special4, Special6drill, Sp8speedDP, Sp7speedProj, Super1RTL},
  {StanceLight, StanceMed, StanceHeavy, StanceFDash, Special1}
};
  fillActionsList(ActTab);
}

  void drawBars(int mult){
    super.drawBars(mult);
    for(int i = 0; i < 3; i++){
      fill(50, 200, 10);
      if(i+1 <= gearLevel) fill(250, 200, 10);
      rect(int(Camerabox.x) - mult*(initWidth/4 + 22*i), GROUNDHEIGHT-10 , 20, 20);
    }
    text( Force.x + ":KM/H", int(Camerabox.x) - mult*(initWidth/4), GROUNDHEIGHT-10 );
  }

  void standingStateReturn(Fighter Opp){
    if(stance){
      CollisionBox.ho = 100;
      CurAction.playAction2(this, Opp, Stance);
    }
    else if(!stance){
      super.standingStateReturn(Opp);
        CollisionBox.ho = 150;
      }
    
      if(y >= GROUNDHEIGHT || CollisionBox.bottom){
        curAirActions = maxAirActions;
      }
          if(CurAction == InHitStun || CurAction == softKD || CurAction == Knockdown){
      stance = false;
    }

  }
  
  void fighterActions(Fighter Opp){
    
    if(!stance)super.fighterActions(Opp);
    fighterActionsExtra();
  }
  
  void fighterActionsExtra(){
     Force.x = constrain(Force.x, -100, 100);
     float l_fx = abs(Force.x);
     if(CurAction.attRank != Action.HITSTATE){
     if(l_fx >= 30 && gearLevel <= 3){ gearLevel = 3; maxAirActions = 3;}
     else if(l_fx >= 20 && gearLevel <= 2){ gearLevel = 2; maxAirActions = 2;}
     else if(l_fx >= 10 && gearLevel <= 1){ gearLevel = 1; maxAirActions = 1;}
     }
     if(gearLevel == 0){ maxAirActions = 1;}
     if(l_fx >= 20){ gearTime = 0;}
     
     gearTime++; 
     if(gearTime >= 800){ 
     gearTime = 0;
     if(gearLevel > 0) gearLevel--;
   }
   
   if(gearLevel >= 3){
     if(curSuper <= maxSuper) curSuper++;
     if(!stance && new comfPButC(new int[][]{{2, 2}} ).cond(this, this)){
       stance = true;
       inputbufferDir.set(inputbufferDir.size()-1, 5);
     }
 }
   
   if(gearLevel >= 3){FDash = fDash_gl3; BDash = bDash_gl3;} 
   else {FDash = fDash_gl0; BDash = bDash_gl0;}
    
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
        
    checkSingleActList(this, ActionList.get(4));
    
         int[] Stmotion = {2, 2}; 
    if( compareBufferWithCombAtt(Stmotion)){
      stance = false;
      //changeAction( Standing);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
    }
       
  }
  
  void st_Normals(){
/*
   if(inputs[5] && firstPressInp[5] && (Force.x >= 6 || Force.x <= -6) && CurAction.attWeight < Special1.attWeight){
          stance = true;
      changeAction(Special1);    
    }*/

  }
  
  void cr_Normals(){
  }
  
  void j_Normals(){
  }
 
  
}

//Plumsklo###############################################################################################################################################################################################
class F_PHaus extends Fighter{
  int curFartMeter = 0, maxFartMeter = 90;
  
  Action st_fHeavy, j_MidNormal2;
  Action BaseNormal, RoofNormal;
  Action Special1, Special3l1, Special3l2, Special3l3, Special4l1, Special4l2, Special4l3, Special5, Special6, Super1;
  PHaus_Sp2 Special2;
  Animation FB1Anim, FB1DestrEff, FartEff0, FartEff1, FartEff2;
  
    Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, st_fHeavy, BaseNormal, RoofNormal, FDash, BDash, Jumping, fDiaJump, bDiaJump, Super1},
  {cr_LightNormal, cr_MidNormal, cr_HeavyNormal, Super1},
  {j_LightNormal, j_MidNormal, j_MidNormal2, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2},
  {Special1,Special2, Special3l1, Special3l2, Special3l3, Special4l1, Special4l2, Special4l3, Super1}
};
  
  F_PHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
        int[] times0 = {60, 4, 4, 4, 4, 4, 4};
      Animation Anim0 = new Animation(times0, 0, 7, "PHouse/PH-st/PH-st");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_PHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
    F_PHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
    F_PHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
 
  F_PHaus(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
  }
  
void specSetup(){
   CollisionBox = new ColRect(0, 0, 150, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
    int[] etimes0 = {10, 10, 10, 10};
    int[] etimes1 = {3, 3, 5, 5, 3, 3, 3, 3};
  FB1Anim = new Animation(etimes0, 0, 4, "Projectiles/Apple/PH-projApple");
  FB1DestrEff = new Animation(etimes1, 0, 0, 8, "Effekte/BettDestrEff/FH-BettEff");
  FartEff0 = new Animation(new int[]{2, 2, 2, 2, 2, 2, 2} , 0, 0, 7, "Effekte/PhEff/PH-1spFartEff");
  FartEff1 = new Animation(new int[]{2, 2, 2, 2, 2, 2, 2}, 0, 0, 7, "Effekte/PhEff/PH-2spFartEff");
  FartEff2 = new Animation(new int[]{2, 2, 2, 2, 2, 2, 2, 2}, 0, 0, 8, "Effekte/PhEff/PH-3spFartEff");
  
  int[] times0 = {60, 4, 4, 4, 4, 4, 4};
  Animation Anim0 = new Animation(times0, 0, 7, "PHouse/PH-st/PH-st");
      Standing = new Action("FH-st-idle", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
      
  Falling.AttAnim = new Animation(new int[]{4}, 0, 90, 1, "PHouse/PH-jumpfall/PH-jfall");
  Landing.AttAnim = new Animation(new int[]{3}, 0, 1, "PHouse/PH-landing/PH-landing");      
  Turning.AttAnim = new Animation(new int[]{3,3,3},  0, 3, "PHouse/PH-turning/PH-turning");
        
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, 0, 6, "PHouse/PH-fWalk/PH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 4, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2.8, 0);
  
  int[] times3 = {2, 1, 2, 1, 1};
  Animation Anim3 = new Animation(times3, 0, 90, 5, "PHouse/PH-njump/PH-njump");
  Anim3.loop = false;
  Jumping = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  Jumping.resetAnim = true;

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  Jumping.addAllLists(1, 2, 0, -20);
  
  fDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  fDiaJump.resetAnim = true;

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
      fDiaJump.addAllLists(1, 2, 11, -20);
  
  bDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  bDiaJump.resetAnim = true;

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bDiaJump.addAllLists(1, 2, -7.5, -20);
  
  Jumping2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  
  FDash = new Action(new Animation(new int[]{2, 3, 5, 2}, 0, 4, "PHouse/PH-dash/PH-fdash"),
  10, 5, 3, Action.MID, false, false, false, false);
  
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 8, 0);
    FDash.addAllLists(1, 8, 10, -4);
    
  BDash = new Action(new Animation(new int[]{4, 4, 2}, 0, 3, "PHouse/PH-dash/PH-bdash"), 
  10, 5, 3, Action.MID, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, 0, 0);
  BDash.addAllLists(1, 5, -8, 0);
  
    airFDash = new Action(new Animation(new int[]{2, 3, 5, 2}, 0, 4, "PHouse/PH-dash/PH-fdash"),
  10, 5, 3, Action.MID, false, false, false, false);
  airFDash.allButSelfCancel = true;
    airFDash.updFrameDataArr(0, 5);
    airFDash.updFrameDataArr_float(0, 8, 0);
    airFDash.addAllLists(1, 8, 18, 0);
    
  airBDash = new Action(new Animation(new int[]{4, 4, 2}, 0, 3, "PHouse/PH-dash/PH-bdash"), 
  10, 5, 3, Action.MID, false, false, false, false);
  
  airBDash.updFrameDataArr(0, 5);
  airBDash.updFrameDataArr_float(0, 0, 0);
  airBDash.addAllLists(1, 5, -12, 0);
  
    int[] times2 = {5};
  Crouching = new Action("ph-cr", new Animation(times2, 0, 1, "PHouse/PH-cr/PH-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "PHouse/PH-Stun/PH-HHitSt"); LHit = new Animation(times1, 0, 1, "PHouse/PH-Stun/PH-LHitSt");
  HBlock = new Animation(times1, 0, 1, "PHouse/PH-Stun/PH-HBlockSt"); LBlock = new Animation(times1, 0, 1, "PHouse/PH-Stun/PH-LBlockSt");
  
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
  
  /*int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD");
  //softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-hKD/FH-hKD");*/

    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  
    int[] times14 = {2, 2, 5, 2, 2};
  Condition[] Cond1 = {new fPButCond(4)};    
  Animation Anim14 = new Animation(times14, 20, 5, "PHouse/PH-st-light/PH-st-light");
  LightNormal = new Action("PH-st-light", Anim14, Cond1, 10, 5, 3, Action.MID, 1, true, false, false, false);
  LightNormal.ActEffs = new ActEffect[]{new SoundEff(6, 0, 0), new CounterEff(1, new CounterHit() )}; //GEFAHR

  Condition[] Cond2 = {new fPButCond(6)};
    int[] times16 = {4, 4, 4, 4, 4, 4};
  Animation Anim16 = new Animation(times16, 75, 6,"PHouse/PH-st-med/PH-st-med");
  MidNormal2 = new Action("Ph-st-med", Anim16, Cond2, 18, 10, 15, 3, 3, true, false, false, false);
  
  Condition[] Cond3 = {new fPButCond(5)};
    int[] times17 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};
    Animation Anim17 = new Animation(times17, 85, 11,"PHouse/PH-st-heay/PH-st-heay");
  HeavyNormal = new Action("Ph-st-heay", Anim17, Cond3, 24, 16, 20, Action.MID, 5, true, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.STAGGER, new CounterHit() )};
  
  int[] times18 = {2, 2, 2, 4, 2};
  Animation Anim18 = new Animation(times18, 25, 5, "PHouse/PH-cr-light/PH-cr-light");
  cr_LightNormal = new Action("Ph-cr-light", Anim18, Cond1, 9, 5, 3, 3, 2, true, false, false, false);

  int[] times19 = {6, 4, 4, 4, 4, 4, 4, 4};
  Animation Anim19 = new Animation(times19, 45, 8, "PHouse/PH-cr-med/PH-cr-med");
  cr_MidNormal = new Action("Ph-cr-med", Anim19, new Condition[]{new ButCond(1), new fPButCond(8)}, 60, 10, 10, Action.LOW, 10, true, true, true, true);
  cr_MidNormal.ActEffs = new ActEffect[]{new KDownEff(0, 0, false), new KDownEff(2, 1, true)};
  
  cr_MidNormal2 = new Action("Ph-cr-m2", new Animation(new int[]{4, 4, 2, 4, 3, 3, 3}, 55, 7, "PHouse/PH-cr-m2/PH-cr-2m"),
  Cond2, 20, 14, 10, Action.MID, 4, true, false, false, false);
  
    int[] times23 = {4, 4, 4, 4, 2, 4, 6, 6, 4, 2}; //40
  cr_HeavyNormal = new Action( "Ph-cr-heay", new Animation(times23, 25, 10, "PHouse/PH-cr-heay/PH-cr-heay"), Cond3,
                               16, 8, 24, Action.MID, 10, true, false, false, false);
  cr_HeavyNormal.jumpCancel = true;
  cr_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.JUGGLE, new CounterHit())};
  
  int[] times20 = {2, 1, 4, 2, 1};
  Animation Anim20 = new Animation(times20, 25, 90, 5, "PHouse/PH-j-light/PH-j-light");
  j_LightNormal = new Action("Ph-j-light", Anim20, Cond1, 9, 4, 3, Action.AIR, 1, true, true, false, false);
  j_LightNormal.jumpCancel = true; j_LightNormal.dashCancel = true;
  
  int[] times22 = {2, 2, 2, 3, 4, 3, 2};
  j_MidNormal2 = new Action( "PH-j-med", new Animation(times22, 65, 90, 7, "PHouse/PH-j-med/PH-j-med"),
            Cond2, 12, 7, 10, Action.AIR, 4, true, true, false, false);
            j_MidNormal2.jumpCancel = true; j_MidNormal2.dashCancel = true;
  j_MidNormal2.ActEffs = new ActEffect[]{new CounterEff(Action.GROUNDBOUNCE, new CounterHit() )};
  
  int[] times21 = {2, 4, 3, 2, 4, 3, 2, 3};
  Animation Anim21 = new Animation( times21, 95, 110, 8, "PHouse/PH-j-heavy/PH-j-heavy");
  j_HeavyNormal = new Action("Ph-j-heay", Anim21, Cond3, 18, 8, 20, Action.AIR, 4, true, true, false, false);
  j_HeavyNormal.jumpCancel = true; j_HeavyNormal.dashCancel = true;
  
  int[] times24 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4};
  int[][] DPvars = {{2, 1, 4}};
  Condition[] Cond4 = {new comfPButC(DPvars), new fPButCond(4)};
  Special1 = new PH_sp1("PH-special1", new Animation( times24, 0, 10, "PHouse/PH-special1/PH-1special"), Cond4, 18, 16, 6, Action.MID, 20, true, true, true, true, this);
  int[] times30 = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
  Condition[] Cond30 = {new comfPButC(DPvars), new fPButCond(6)};
  Special2 = new PHaus_Sp2("PH-special2", new Animation( times30, 0, 10, "PHouse/PH-special2/PH-2special"), Cond30, 6, 2, 6, Action.MID, 20, true, true, true, true,
  1.5, 400, 0, 0, true, true, false, this);
  Special2.ProjAnim = FB1Anim;
  Special2.destrEffAnim = FB1DestrEff;
  Special2.gravMult = 0.5;
   
  Condition[] Cond5 = {new facingCond(1), new fPButCond(5)};
   int[] times25 = {4, 3, 6, 2, 3, 6, 3, 3, 3};
   st_fHeavy = new Action("FH-st-forH",//"data/FH-st-fHeavy", 
   new Animation( times25, 75, 9, "FHouse/FH-st.fH/FH-st-fH"), Cond5, 18, 14, 6, Action.HIGH, 12, true, false, false, false);
      
    int[] times21p2 = {2, 2, 2, 2};
    Animation Sp3p2Anim = new Animation(times21p2, 70, 100, 4,"PHouse/PH-special3/PH-3special1p");
    Action Special3p2 = new Action(// "FH-sp3p2",
    Sp3p2Anim, 0, 0, 0, Action.MID, true, true, true, false);
    
         Action FillAct = Special3p2;
    FillAct.updFrameDataArr(0, 8);
  //FillAct.updFrameDataArr_float(0, 16, 18);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -100, 200, 200));
    
    int[] times21p1 = {2, 2, 2, 2, 4, 2, 4, 2, 4, 2};
    Animation Sp3p1Anim = new Animation(times21p1, 70, 100, 10,"PHouse/PH-special3/PH-3special1p");
    
    int[][] revFBvars = {{2, 3, 6}, {2, 3, 6, 7}};
    Condition[] Cond6 = {new Grounded(), new comfPButC(revFBvars), new fPButCond(5), new PH_FartCond(0, 5, this)};
    Special3l1 = new ChangeOnEndAct( "Ph-special3-2",
    Sp3p1Anim, 12, 8, 12, Action.MID, true, true, false, false, Special3p2); 
    Special3l1.Conds = Cond6; Special3l1.attWeight = 30;
          FillAct = Special3l1; FillAct.updFrameDataArr_float(1, 3, -15);
         Special3l1.ActEffs = new ActEffect[]{ new AddVisEff(FartEff1, 0, -120, 1, new ActTimeCond2(0, 0) ) };
   
   Condition[] Cond6_2 = {new Grounded(), new comfPButC(revFBvars), new fPButCond(5), new PH_FartCond(30, 15, this)};
    Special3l2 = new ChangeOnEndAct( "Ph-special3-2",
    Sp3p1Anim, 20, 14, 20, Action.MID, true, true, true, false, Special3p2); 
    Special3l2.Conds = Cond6_2; Special3l2.attWeight = 30;
          FillAct = Special3l2; FillAct.updFrameDataArr_float(1, 4.4, -22);
          Special3l2.ActEffs = new ActEffect[]{ new AddVisEff(FartEff1, 0, -120, 1, new ActTimeCond2(0, 0) ) };
   
      Condition[] Cond6_3 = {new Grounded(), new comfPButC(revFBvars), new fPButCond(5), new PH_FartCond(60, 20, this)};
    Special3l3 = new ChangeOnEndAct( "Ph-special3",
    Sp3p1Anim, 20, 18, 12, Action.MID, true, true, true, true, Special3p2); 
    Special3l3.Conds = Cond6_3; Special3l3.attWeight = 30;
          FillAct = Special3l3; FillAct.updFrameDataArr_float(1, 5.6, -25);
          Special3l3.ActEffs = new ActEffect[]{ new AddVisEff(FartEff1, 0, -120, 1, new ActTimeCond(0, 0, 1,0) ) };
   
       int[] times26p2 = {2, 2, 2, 4, 2};
    Animation Sp4p2Anim = new Animation(times26p2, 0, 5,"PHouse/PH-special4/PH-4special2p");
    Action Special4p2 = new Action(// "FH-sp3p2",
    Sp4p2Anim, 0, 0, 0, Action.MID, true, false, true, false);
    Sp4p2Anim.loop = false;
    
         FillAct = Special4p2;
    FillAct.updFrameDataArr(0, 12);
   FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -100, 200, 200));
    
    int[] times26p1 = {3, 4, 2, 3, 60};
    Animation Sp4p1Anim = new Animation(times26p1, 0, 5,"PHouse/PH-special4/PH-4special1p"); Sp4p1Anim.loop = false;
    
       int[][] halfCircM = {{4, 1, 2, 3, 6}, {4, 2, 6}, {4, 1, 2, 3, 6, 7}, {2,3,6}};
   Condition[] Cond7_1 = {new comfPButC(halfCircM), new fPButCond(6), new PH_FartCond(0, 5, this)};
  Special4l1 = new ChgOEndAndHit("PH-special4l1", Sp4p1Anim, Cond7_1, 10, 8, 10, Action.MID, 20, true, false, false, false, Special4p2);
            FillAct = Special4l1; FillAct.updFrameDataArr_float(1, 16, 0);
            Special4l1.ActEffs = new ActEffect[]{ new AddVisEff(FartEff0, -120, -110, 1, new ActTimeCond2(0, 0) ) };
   
      Condition[] Cond7_2 = {new comfPButC(halfCircM), new fPButCond(6), new PH_FartCond(30, 8, this)};
  Special4l2 = new ChgOEndAndHit("PH-special4l2", Sp4p1Anim, Cond7_2, 18, 14, 14, Action.MID, 20, true, false, false, false, Special4p2);
            FillAct = Special4l2; FillAct.updFrameDataArr_float(1, 20, 0);
            Special4l2.ActEffs = new ActEffect[]{ new AddVisEff(FartEff0, -120, -110, 1, new ActTimeCond2(0, 0) ) };
            
               Condition[] Cond7_3 = {new comfPButC(halfCircM), new fPButCond(6), new PH_FartCond(60, 14, this)};
  Special4l3 = new ChgOEndAndHit("PH-special4l3", Sp4p1Anim, Cond7_3, 25, 23, 18, Action.MID, 20, true, false, false, false, Special4p2);
            FillAct = Special4l3; FillAct.updFrameDataArr_float(1, 28, 0);
            Special4l3.ActEffs = new ActEffect[]{new CounterEff(1, new Condition[]{new CounterHit()})}; //GEFAHR
            Special4l3.ActEffs = new ActEffect[]{ new AddVisEff(FartEff0, -120, -110, 1, new ActTimeCond2(0, 0) ) };
            
      int[] times1p3 = {2, 2, 2, 2};
    Action Special5p3 = new Action( //"HH-cr-Light",
    new Animation(times1p3, 0, 100, 4, "PHouse/PH-special5/PH-5sp3p"), 6, 5, 4, Action.AIR, true, false, false, false);
    Special5p3.gravMult = 0.8;
    FillAct = Special5p3;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
    FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -90, 180, 180));
    
    int[] times1p2 = {2, 2, 2, 2, 2, 2, 2, 2};
    Action Special5p2 = new HoldButToKeepAct( "Ph-special5p2",
    new Animation(times1p2, 0, 100, 8, "PHouse/PH-special5/PH-5sp2p"), 6, 5, 4, Action.AIR, true, true, false, false, Special5p3, 40, 0);
    Special5p2.gravMult = 0.7;
    
    int[] times1p1 = {2, 2, 2, 2};
    Special5 = new ChangeOnEndAct( //"HH-cr-Light",
    new Animation(times1p1, 0, 100, 4, "PHouse/PH-special5/PH-5sp1p"), 6, 5, 4, Action.AIR, true, true, false, false, Special5p2); 
    Special5.Conds = new Condition[]{new InAir(),  new fPButCond(0), new PH_FartCond(15, 15, this)}; Special5.attWeight = 3; Special5.gravMult = 0.85;
          FillAct = Special5;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, -20);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -90, 180, 180));
   
     int[] times26 = {4, 2, 4, 2, 3, 2, 2, 3, 2, 2, 2, 2};
  int[][] FB = {{2, 3, 6}}, revFB = {{2, 1, 4}};
  // new UseEffCond(Special6, new Condition[]{ new comfPButC(FB), new fPButCond(5) }, new ActEffect[]{new PH_dirEff(1, new Condition[]{}, -20, -20, 180, -1)} 
  Special6 = new Action("PH-special6", new Animation( times26, 0, 100, 12, "PHouse/PH-special6/PH-6sp"), new Condition[]{}, 22, 16, 6, Action.AIR, 30, false, false, false, false);
           Special6.AttAnim.rot = 0; FillAct = Special6;
    FillAct.updFrameDataArr_float(2, 30, 0);
      Special6.Conds = new Condition[]{new InAir(),
      
      new OrCond(
            new OrCond( 
    new UseEffCond(Special6, new Condition[]{ new comfPButC(FB), new fPButCond(5) }, new ActEffect[]{ new AddVisEff(FartEff2, -120, -100, 1, new ActTimeCond2(0, 0) ), new PH_dirEff(1, new Condition[]{}, 22, -10, TWO_PI-PI/8, -1), new AddOwnForcToOpp(2,1, new Condition())}),
    new UseEffCond(Special6, new Condition[]{ new comfPButC(FB), new fPButCond(4) }, new ActEffect[]{ new AddVisEff(FartEff2, -120, -100, 1, new ActTimeCond2(0, 0) ), new PH_dirEff(1, new Condition[]{}, 30, 0, 0, -1), new AddOwnForcToOpp(2,1.4, new Condition())})
    ),
            new OrCond( 
    new UseEffCond(Special6, new Condition[]{ new comfPButC(revFB), new fPButCond(5) }, new ActEffect[]{ new AddVisEff(FartEff2, -120, -100, 1, new ActTimeCond2(0, 0) ), new PH_dirEff(1, new Condition[]{}, 22, 10, PI/8, -1), new AddOwnForcToOpp(2,1, new Condition()) }),
    new UseEffCond(Special6, new Condition[]{ new comfPButC(revFB), new fPButCond(4) }, new ActEffect[]{ new AddVisEff(FartEff2, 200, -100, 1, new ActTimeCond2(0, 0) ), new PH_dirEff(1, new Condition[]{}, -30, 0, PI, -1), new AddOwnForcToOpp(2,1, new Condition()) })
    )
    ),     
 new PH_FartCond(15, 15, this)}; //needed for dirAff: fx, fy, dirMult, hurt- and hitboxen
 Special6.selfCancel = true;
                   
   int[][] superm = {{2, 3, 6, 2, 3, 6}, {2, 3, 6, 2, 3}, {2, 6, 2, 6}, {2, 3, 6, 2, 3, 6, 9}};
       Condition[] Cond8 = {new SuperCond(500), new comfPButC(superm), new fPButCond(6), new PH_FartCond(0, 30, this)};
     int[] times27= {3, 3, 3, 3, 3,
   3, 3, 3, 3, 3,
 3, 3, 3, 3, 3, 3};
  Super1 = new Action("FH-1super", new Animation( times27, 95, 16, "FHouse/FH-1super/FH-1super"), Cond8, 20, 18, 10, Action.MID, 50, true, false, false, true);
   Super1.superNeed = 500;
   
    Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, st_fHeavy, FDash, BDash, Jumping, fDiaJump, bDiaJump, Special1, Special2, 
   Special3l3, Special3l2, Special3l1, Special4l3, Special4l2, Special4l1, Super1, NormalThrow},
  {cr_LightNormal, cr_MidNormal2, cr_MidNormal, cr_HeavyNormal, AirThrow, Special1, Super1},
  {j_LightNormal,j_MidNormal2, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2, Special2, Special5, Special6},
  {Special1, Special2, Special3l3, Special3l2, Special3l1, Special4l3, Special4l2, Special4l1, Super1, Special6}
};

  fillActionsList(ActTab);
   
}

  void drawBars(int mult){
    float centerZ2 = 0 / tan(PI*30.0 / 180.0);
    drawBar( int(Camerabox.x) - mult*(initWidth/4), 20 - int(0) , centerZ2, maxHP, curHP, mult * -180);
    drawBar( int(Camerabox.x) - mult*(initWidth/4), GROUNDHEIGHT+10 - int(0) , centerZ2, maxSuper, curSuper, mult * -140);
    drawBar( int(Camerabox.x) - mult*(initWidth/4), GROUNDHEIGHT-10 - int(0) , centerZ2, maxFartMeter, curFartMeter, mult * -90);
  } 

  void st_Normals(){
  }
  void cr_Normals(){
  }
  void j_Normals(){
    }   
   
  void fighterActionsExtra(){
    curFartMeter = constrain(curFartMeter, 0, maxFartMeter);
  }
  
    // "0000 (Dir) 000 (AttButs) 0 (RC) : timeactive"
  
  InputRecord FBl = new InputRecord(this, 2, true, 
  "0100 000 0 : 3",
  "0110 000 0 : 3",
  "0010 000 0 : 3",
  "0010 100 0 : 3",
  "0010 100 0 : 3"
  );
   InputRecord FBm = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0110 000 0 : 2",
  "0010 000 0 : 2",
  "0010 001 0 : 2",
  "0010 001 0 : 2"
  );
   InputRecord FBh = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0110 000 0 : 2",
  "0010 000 0 : 2",
  "0010 010 0 : 2",
  "0010 010 0 : 2"
  );
  InputRecord rFBh = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0101 000 0 : 2",
  "0001 000 0 : 2",
  "0001 010 0 : 2",
  "0001 010 0 : 2"
  );
  InputRecord rFBl = new InputRecord(this, 2, true, 
  "0100 000 0 : 3",
  "0101 000 0 : 3",
  "0001 000 0 : 3",
  "0001 100 0 : 3",
  "0001 100 0 : 3"
  );
  InputRecord Doublf = new InputRecord(this, 2, true,
  "0010 000 0 : 2",
  "000 000 0 : 1",
  "0010 000 0 : 2",
  "0010 000 0 : 2"
  );

  
  void AIControll3(Fighter Opp){
    float aggro = dist(x, y, Opp.x, Opp.y)*0.1 + (Opp.maxHP/(Opp.curHP+1)) - (curHP/(maxHP+1));
    float distxy = dist(x, y, Opp.x, Opp.y), distx = dist(x, 0, Opp.x, 0), disty = dist(0, y, 0, Opp.y);
    
    if((distx > 420 || Opp.CurAction == Opp.Knockdown) && aggro >= 10 && curFartMeter < 70){
      Recorder = rFBl;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx <350 && disty > 60 && aggro >= 30 && curFartMeter > 35){
      Recorder = FBh;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx <370 && disty > 220 && aggro >= 40 && new InAir().cond(this, Opp) && curFartMeter >= 15){
      Recorder = rFBh;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx > 600 && aggro >= 60 && curFartMeter >= 30){
      Recorder = FBm;    
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }
    else
    if(distx > 660 && aggro >= 60){
      Recorder = Doublf;    
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }
    else
    if(distx > 400 && aggro >= 20 && new InAir().cond(this, Opp) && curFartMeter >= 15){
      Recorder = FBlightR;    
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }
    else Recorder = null;

  }
  
  
}

//Housefighter Endboss###############################################################################################################################################################################################
class F_HFBHaus extends Fighter{

  Action st_fHeavy, j_MidNormal2, st_punch, st_slide;
  
 F_HFBHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "HFB/HFB-st/HFB-st");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_HFBHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
 F_HFBHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
 F_HFBHaus(int x, int y, PlControl Con){
    super(x,  y, Con);
  }
  
 F_HFBHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
  
void specSetup(){
  name = "HFBHouse";
   CollisionBox = new ColRect(0, 0, 150, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "HFB/HFB-st/HFB-st");
      Standing = new Action("FH-st-idle", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
        
  int[] times1 = {6, 6, 6, 6, 6};
  Animation Anim1 = new Animation(times1, 0, 5, "HFB/HFB-fwalk/HFB-fwalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 3, 0);//3, 0);
  
  bWalk = new Action(new Animation(times1, 0, 5, "HFB/HFB-bwalk/HFB-bwalk"), false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2, 0);
  
  int[] times3 = {1, 2, 1, 4};
  Animation Anim3 = new Animation(times3, 0, 4, "FHouse/FH-n.jump/FH-n.jump");
  Anim3.loop = false;
  Jumping = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  Jumping.resetAnim = true;

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  Jumping.addAllLists(1, 2, 0, -20);
  
  fDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  fDiaJump.resetAnim = true;

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
      fDiaJump.addAllLists(1, 2, 11, -20);
  
  bDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  bDiaJump.resetAnim = true;

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bDiaJump.addAllLists(1, 2, -7.5, -20);
  
  Jumping2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  
  FDash = new Action(new Animation(new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4}, 40, 9, "HFB/HFB-run/HFB-run2p"), 0, 0, 0, 0, true, false, false, false);
  FDash.ActEffs = new ActEffect[]{new setActCurColTime(1, 0, new ActTimeCond2(1, 2), new facingCond(1) ), new SetForce(12, 0, 1, new ActTimeCond2(1, 1))};
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 0.5, 0);
    FDash.addAllLists(1, 2, 12, 0);
    FDash.addAllLists(2, 8, 2, 0);
    
  int[] ani5 = {1, 10};
  BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, -8, 0);
  BDash.addAllLists(1, 5, -10, 0);
  
    int[] times2 = {5};
  Crouching = new Action("fh-cr", new Animation(times2, 40, 1, "HFB/HFB-cr/HFB-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  Landing.AttAnim = new Animation(new int[]{4}, 40, 1, "HFB/HFB-landing/HFB-landing");
  
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

  
  /*int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD");
  //softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-hKD/FH-hKD");*/

  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);

  st_punch = new Action("HFB-punch", new Animation(new int[]{8, 8, 3, 4, 8, 4, 8}, 60, 10, 7,"HFB/HFB-punch/HFB-punch"), 
  new Condition[]{new fPButCond(4)}, 20, 12, 12, 3, 3, true, false, false, false);
  st_punch.ActEffs = new ActEffect[]{new CounterEff(0)};
    
  st_slide = new Action("HFB-slide", new Animation(new int[]{6, 6, 4, 3, 8,  3, 12, 4, 8, 4,  3, 8, 12}, 140, 13,"HFB/HFB-slide/HFB-slide"), 
  new Condition[]{new fPButCond(6)}, 20, 12, 12, Action.LOW, 4, true, true, true, false);
  
      
    Action[][] ActTab = {
  {st_punch, st_slide},
  {},
  {},
  { }
};

  fillActionsList(ActTab);
   
}
  void st_Normals(){
        if(CurAction == fWalk){
      Force.x =  dirMult * sqrt(sq(sin(fWalk.AttAnim.timer* 0.1))) * 6;
    }
  }
  
  void cr_Normals(){
  }
  
  void j_Normals(){
    }   
         
}

class F_NPC_0 extends Fighter{
   F_NPC_0(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
 F_NPC_0(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);  tint = true; AI_Controlled = true;
 }
  F_NPC_0(int x, int y, char[] charinputs, ControlDevice device){
   super(x, y, charinputs, device);
 }
 
 void specSetup(){
   basicActSetup();
 }
 
 void basicActSetup(){
    CollisionBox = new ColRect(0, 0, 150, 150);
      Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  int[] times0 = {5, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
        
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, 0, 7, "FHouse/FH-fWalk/FH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 3, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2, 0);
  
  int[] times3 = {1, 2, 1, 4};
  Animation Anim3 = new Animation(times3, 0, 4, "FHouse/FH-n.jump/FH-n.jump");
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
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-hKD/FH-hKD");

  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
}
 
}

class F_Enemy0 extends F_NPC_0{
  Action Special1; Action Special3;

 F_Enemy0(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
 F_Enemy0(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);  tint = true; AI_Controlled = true;
    Special1 = ActionList.get(0).get(0); 
    Special3 = new ChangeOnEndAct(ActionList.get(0).get(1), new ChangeOnCondAct(ActionList.get(0).get(2), new Action (ActionList.get(0).get(3)) ));
 }
 
 F_Enemy0(int x, int y, char[] charinputs, ControlDevice device){
   super(x, y, charinputs, device);
 }
 
void specSetup(){
  super.basicActSetup();
  maxHP = 50; curHP = maxHP;
  
  Condition[] Cond3 = {new fPButCond(5)};
    int[] times17 = {2, 2, 3, 2, 4, 6, 2, 1, 1, 2, 2, 2};
    Animation Anim17 = new Animation(times17, 50, 12,"FHouse/FH-st.Heavy/FH-st.Heavy");
  HeavyNormal = new Action("FH-st-heay", Anim17, Cond3, 16, 12, 20, Action.MID, 5, true, false, false, false);
  
  int[] times24 = {2, 2, 2, 2, 4, 2, 2, 2};
  int[][] DPvars = {{6, 2, 3}, {6, 3, 2, 3, 6}, {6, 3, 2, 3}, {6, 2, 6}};
  Condition[] Cond4 = {new comfPButC(DPvars), new fPButCond(5)};
  Special1 = new Action("FH-sp1", new Animation( times24, 50, 8, "FHouse/FH-sp1/FH-1special"), Cond4, 18, 16, 6, Action.MID, 20, true, true, false, false);
   
    int[] times21p3 = {4, 4, 4, 4, 4};
    Action Special3p3 = new Action( //"HH-cr-Light",
    new Animation(times21p3, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-3p"), 6, 5, 4, 0, true, true, false, false);
    
    Action FillAct = Special3p3;
    FillAct.updFrameDataArr(0, 20);
  FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p2 = {2, 2, 2};
    Action Special3p2 = new ChangeOnCondAct( "FH-sp3p2",
    new Animation(times21p2, 25, 50, 3,"FHouse/FH-sp3/FH-3sp-2p"), 20, 10, 20, Action.HIGH, true, false, true, false, Special3p3);
    
         FillAct = Special3p2;
    FillAct.updFrameDataArr(0, 60);
  FillAct.updFrameDataArr_float(0, 16, 18);
   //FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[][] revFBvars = {{2, 1, 4}, {2, 1, 4, 7}};
    Condition[] Cond6 = {new InAir(), new comfPButC(revFBvars), new fPButCond(4)};
    int[] times21p1 = {2, 2, 2, 2, 2};
    Special3 = new ChangeOnEndAct( //"HH-cr-Light",
    new Animation(times21p1, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-1p"), 6, 5, 4, Action.AIR, false, false, false, false, Special3p2); 
    Special3.Conds = Cond6; Special3.attWeight = 30;
          FillAct = Special3;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
  Action[][] ActTab = { {Special1, Special3, Special3p2, Special3p3},
   {}, {}, {}
  };
  fillActionsList(ActTab);
   
}
 
  void AI_Controll(Fighter Opp){
    if(new Grounded().cond(this, Opp) && CurAction == Standing){
      if( abs(Opp.x - x) <= 300){
        changeAction(Special1);
      }
      else{changeAction(fWalk);}
    }
    else if(new InAir().cond(this, Opp) && CurAction == Standing){
        changeAction(Special3);
    }
    
  }
  
  void AIControll2(Fighter Opp){
  }

  void st_Normals(){
  }
  
  void cr_Normals(){
  }
  
  void j_Normals(){          
  }
  
}

class F_Enemy1 extends F_NPC_0{
  
  F_Enemy1(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
      int[] times0 = {10, 10, 10, 10, 10, 10, 10, 10};
      Animation Anim0 = new Animation(times0, 0, 8, "HHouse/HH-st.idle/HH-st.idle");
      Standing = new Action("HH-st-idle", Anim0, false);
  }
  
   F_Enemy1(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);  tint = true; AI_Controlled = true;
    cr_MidNormal = ActionList.get(0).get(0); Standing = ActionList.get(0).get(1); 
 }
  F_Enemy1(int x, int y, char[] charinputs, ControlDevice device){
   super(x, y, charinputs, device);
 }
 
void specSetup(){
  super.basicActSetup();
      int[] times2 = {5, 10, 10, 10};
  Standing = new Action(new Animation(times2, 0, 4, "FHouse/FH-cr/HF-cr"), false);
  Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
    int[] times19 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  Animation Anim19 = new Animation(times19, 50, 15, "FHouse/FH-cr.Mid/FH-cr.Mid");
  cr_MidNormal = new Action("FH-cr-med", Anim19, new Condition[]{}, 60, 10, 10, Action.LOW, 4, true, true, true, false);
  cr_MidNormal.setForceAtDur[0][0] = 5.0;
  
  CollisionBox = new ColRect(0, 0, 150, 80);
    Action[][] ActTab = { {cr_MidNormal, Standing},
   {}, {}, {}
  };
  fillActionsList(ActTab);
}

  void AI_Controll(Fighter Opp){
    if(new Grounded().cond(this, Opp) && CurAction == Standing){
      if( abs(Opp.x - x) <= 300){
        changeAction(cr_MidNormal);
      }
      //else{changeAction(fWalk);}
    }
    /*else if(new InAir().cond(this, Opp) && CurAction == Standing){
        changeAction(Special3);
    }*/
    
  }
  
  void AIControll2(Fighter Opp){
  }

 
}

//KUGELENEMY###############################################################################################################################################################################################
class F_KEnem extends Fighter{
  Action Fireball;
  
 F_KEnem(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5, 10, 10, 10};
      Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_KEnem(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
      int[] times0 = {5, 10, 10, 10};
      Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_KEnem(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
 F_KEnem(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
  
void specSetup(){
  name = "BallEnemy";
   CollisionBox = new ColRect(0, 0, 80, 80);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  
  int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "KEnem/KE-standing/KE-st");
      Standing = new Action("KE-st", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
        
  int[] times1 = {10, 10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, 0, 8, "KEnem/KE-fwalk/KE-fwalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 100, 100, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 4, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 100, 100, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2.5, 0);
  
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action("fh-cr", new Animation(times2, 0, 4, "FHouse/FH-cr/HF-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "KEnem/hitStun/hitStun"); LHit = HHit; HBlock = HHit; LBlock = HHit;
  
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
  
  /*int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD");
  //softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-hKD/FH-hKD");*/
  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  
    int[] times14 = {3, 2, 5, 2, 3, 3, 3, 3, 3, 3, 2, 3, 3, 3, 3};
  Condition[] Cond1 = {new fPButCond(6)};    
  Animation Anim14 = new Animation(times14, 0, 15, "KEnem/KE-Att2/KE-2Att");
  LightNormal = new Action("KE-Att2", Anim14, Cond1, 16, 5, 4, Action.MID, 1, true, true, false, false);
  LightNormal.ActEffs = new ActEffect[]{};
  
    int[] etimes1 = {3, 3, 5, 5, 3, 3, 3, 3};
       Projectile AcidProj = new Projectile(0.0, 0.0, 0, -80, 40, 40, 7.0, 0.0, 1, 60, 12, 6, 6, false, false, false, true, true) ;
   AcidProj.setAnims(new Animation(new int[]{10}, 0, 0, 1, "Projectiles/AcidShoot/AcidShoot"), new Animation(etimes1, 0, 0, 8, "Effekte/BettDestrEff/FH-BettEff"));
                  int[] times32 = {2, 6, 2, 6, 2,
                2, 2, 6, 2, 2,
              2};
   Fireball = new Action( new Animation( times32, 0, 11, "KEnem/KE-Att1/KE-1Att"), 32, 10, 4, Action.MID, true, false, false, false);
   Fireball.ActEffs = new ActEffect[]{ new ProjAddEff( AcidProj, 1, new ActTimeCond2(1, 1) ),  new ProjAddEff( AcidProj, -1, new ActTimeCond2(1, 1) )}; 
   Fireball.Conds = new Condition[]{new fPButCond(4)}; Fireball.attWeight = 30;
     Fireball.updFrameDataArr(0, 19);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball.addAllLists(1, 25, 0, 0);
  Fireball.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
    
    Action[][] ActTab = {
  {LightNormal, Fireball},
  {},{},{}
};

  fillActionsList(ActTab);
}

void stunStatesAnimSetup(){
      AnimKD = new Animation(new int[]{2, 4, 2, 20} , 0, 4, "KEnem/knockDown/KE-kd"); 
  AnimKD.loop = false;
  AnimKDreturn = new Animation(new int[]{4, 4, 4, 4} , 0, 4,"KEnem/KDreturn/KE-kd"); 
  AnimAirHit = new Animation(new int[]{7, 7, 6} , 0, 3,"KEnem/airHitstun/KE-kd");
  AnimAirHit.loop = false;
}

  void standingStateReturn(Fighter Opp){
    if(y == GROUNDHEIGHT || CollisionBox.bottom) curAirActions = maxAirActions;
    
          if((y >= GROUNDHEIGHT || CollisionBox.bottom) && CurAction.attKind == 4) changeAction(Standing);
      else CurAction.playAction2(this, Opp, Standing); 
  }

  void jump(){} void dash(){}

  void st_Normals(){
  }
  
  void cr_Normals(){
  }
  
  void j_Normals(){
    }   
         
}

//KUGELENEMY###############################################################################################################################################################################################
class F_DEnem extends Fighter{
  Action inAir, ceilingStick;
  
 F_DEnem(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5, 10, 10, 10};
      Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_DEnem(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
          int[] times0 = {5, 10, 10, 10};
      Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_DEnem(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
 F_DEnem(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
  
void specSetup(){
  name = "KegelEnemy";
   CollisionBox = new ColRect(0, 0, 80, 250);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  
  int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "DEnem/DE-standing/DE-st");
      Standing = new Action("KE-st", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
        
  int[] times1 = {10, 10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, 0, 8, "KEnem/KE-fwalk/KE-fwalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 100, 100, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 4, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 100, 100, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2.5, 0);
  
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action("fh-cr", new Animation(times2, 0, 4, "FHouse/FH-cr/HF-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "KEnem/hitStun/hitStun"); LHit = HHit; HBlock = HHit; LBlock = HHit;
  
  int[] ani9 = {6};
  Blocking = new Action(ani9);
  Blocking.updFrameDataArr(0, 2); 
  Blocking.updFrameDataArr_float(0, 0, 0);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 200, 160));
  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);

  inAir = new Action("KE-st", new Animation(times0, 0, 1, "DEnem/DE-inAir/DE-inAir"), false); inAir.addingForce = false;
  ceilingStick = new Action("KE-st", new Animation(times0, 0, 1, "DEnem/DE-stickCeiling/DE-stick"), false); ceilingStick.gravityActive = false; ceilingStick.addingForce = false;
  
  /*  int[] times14 = {3, 2, 5, 2, 3, 3, 3, 3, 3, 3, 2, 3, 3, 3, 3};
  Condition[] Cond1 = {new fPButCond(6)};    
  Animation Anim14 = new Animation(times14, 0, 15, "KEnem/KE-Att2/KE-2Att");
  LightNormal = new Action("KE-Att2", Anim14, Cond1, 16, 5, 4, Action.MID, 1, true, true, false, false);
  LightNormal.ActEffs = new ActEffect[]{};*/
  
  Action j_LightNormalP2 = new Action("DE-Att2p2", new Animation(new int[]{4, 4, 4, 4, 4, 2}, 0, 0, 6, "DEnem/DE-2Att/p2/DE-2Att"), 
  new Condition[]{}, 16, 5, 4, Action.MID, 1, true, false, false, false);
  j_LightNormalP2.ActEffs = new ActEffect[]{};
  
  j_LightNormal = new Action("DE-Att2", new Animation(new int[]{4, 4, 100}, 0, 60, 3, "DEnem/DE-2Att/DE-2Att"), 
  new Condition[]{new InAir(), new fPButCond(5)}, 16, 5, 4, Action.HIGH, 1, true, false, false, false);
  j_LightNormal.ActEffs = new ActEffect[]{new ChangeActTo(j_LightNormalP2, 1, new Grounded())}; j_LightNormal.collision = false;
  
  j_MidNormal = new Action("DE-Att4", new Animation(new int[]{8, 4, 6, 4, 4, 4, 4}, 0, 350, 7, "DEnem/DE-Att4/DE-4Att"), 
  new Condition[]{new fPButCond(6)}, 16, 5, 4, Action.HIGH, 1, false, false, false, false);
  j_MidNormal.ActEffs = new ActEffect[]{}; j_LightNormal.collision = false;
  
  MidNormal2 = new Action("DE-Att1", new Animation(new int[]{6, 4, 2, 4, 2, 4, 2, 4, 4, 2}, 0, 10, "DEnem/DE-1Att/DE-1Att"), 
  new Condition[]{new fPButCond(5)}, 16, 5, 4, Action.MID, 1, true, true, false, false);
  MidNormal2.ActEffs = new ActEffect[]{};
  
  HeavyNormal = new Action("DE-Att3", new Animation(new int[]{4, 4, 4, 4, 6, 6, 4, 4, 4, 4}, 50, 10, "DEnem/DE-Att3/DE-3Att"), 
  new Condition[]{new fPButCond(4)}, 16, 5, 4, Action.MID, 1, true, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{};
    
    Action[][] ActTab = {
  {HeavyNormal, MidNormal2, j_MidNormal},
  {},{j_LightNormal},{}, 
  {j_LightNormal}, //InAirState
  {j_LightNormal, j_MidNormal}  //ceilingState
};

  fillActionsList(ActTab);
}

void fighterActionsExtra(){
    if(ceilStick){ checkSingleActList(this, ActionList.get(5)); if(inputs[0] && inputs[4]) ceilStick = false;}
    else if(y < GROUNDHEIGHT && !CollisionBox.bottom) checkSingleActList(this, ActionList.get(4));
           
}

void stunStatesAnimSetup(){
      AnimKD = new Animation(new int[]{2, 4, 2, 20} , 0, 4, "KEnem/knockDown/KE-kd"); 
  AnimKD.loop = false;
  AnimKDreturn = new Animation(new int[]{2, 2, 2, 2} , 0, 4,"KEnem/KDreturn/KE-kd"); 
  AnimAirHit = new Animation(new int[]{7, 7, 6} , 0, 3,"KEnem/airHitstun/KE-kd");
  AnimAirHit.loop = false;
}

boolean ceilStick = false;
  void standingStateReturn(Fighter Opp){
    if(CollisionBox.top){ changeAction(ceilingStick); ceilStick = true;}
    if(y == GROUNDHEIGHT || CollisionBox.bottom || CurAction == j_LightNormal) ceilStick = false;
    if(y == GROUNDHEIGHT || CollisionBox.bottom) curAirActions = maxAirActions;
    
          if((y >= GROUNDHEIGHT || CollisionBox.bottom) && CurAction.attKind == 4) changeAction(Standing);
          else if(ceilStick) CurAction.playAction2(this, Opp, ceilingStick);
          else if(y < GROUNDHEIGHT && !CollisionBox.bottom) CurAction.playAction2(this, Opp, inAir);
      else CurAction.playAction2(this, Opp, Standing); 
  }

  void jump(){} void dash(){}

  void st_Normals(){
  }
  
  void cr_Normals(){
  }
  
  void j_Normals(){
    }   
         
}

class F_Edit extends Fighter{
  String F_datnam = "none";
  void specSetup(){}
} 

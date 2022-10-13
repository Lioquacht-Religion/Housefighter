static class Structs{
  private Structs(){}
}

class PlControl extends Structs{
    ControlDevice device = null; ControlButton LightBut, MediumBut, HeavyBut, RCBut, EMBut; ControlHat DPad; ControlSlider LStick_X, LStick_Y; boolean[] pInputs = new boolean[4];
  boolean[] inputs = new boolean[9]; //up , down, right, left, Roof, Mid, Base, utility(RC), Special;
  boolean[] firstPressInp = new boolean[inputs.length];
  boolean[] firstPressDia = {true, true, true, true};
  int[] inputChargeT = new int[inputs.length];
  char[] charinputs;
  
  PlControl(){device = null; charinputs = new char[]{'w', 's', 'd', 'a', 'j', 'i', 'o', 'p', 'n'};}
  PlControl(ControlDevice device){this.device = device; if(device != null)setConDevice(); charinputs = new char[]{'w', 's', 'd', 'a', 'j', 'i', 'o', 'p', 'n'};}
  PlControl(char[] charinputs){this.device = null; this.charinputs = charinputs;}
  PlControl(ControlDevice device, char[] charinputs){this.device = device; if(device != null)setConDevice(); this.charinputs = charinputs;}
  
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
  
   void deviceInput(){

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
      inputs[8] = EMBut.pressed();
    }
    
    
  }
  
  void searchDevice(){
       control.getDevices().clear();
      println(control.deviceListToText("") );
      println("searching for devices now");
        
         Player1.PlContr.device = null; 
         Player2.PlContr.device = null; 
         control = ControlIO.getInstance(p);
         Player1.PlContr.device = control.getMatchedDeviceSilent("HFcontroller"); 
         Player2.PlContr.device = control.getMatchedDeviceSilent("HFcontroller"); 
         if(Player1.PlContr.device != null) {//println( device.getName()+ " " + device.getPortTypeName() ); 
          Player1.PlContr.setConDevice(); println("connect");}
         if(Player2.PlContr.device != null) {//println( device.getName()+ " " + device.getPortTypeName() ); 
          Player2.PlContr.setConDevice(); println("connect");}
         
         println(control.deviceListToText("") );
  }
  
  
  void draw(){
        //chargeinputs
    for(int i = 0; i < inputs.length; i++){
      if(inputs[i] && inputChargeT[i] <= 1000) inputChargeT[i]++;
      else if(inputChargeT[i] > 10) inputChargeT[i] -= inputChargeT[i]/10;
      else inputChargeT[i] = 0;     
    }
   //chargeinputs
   
   for(int i = 0; i < firstPressInp.length; i++){
      if(!inputs[i]) firstPressInp[i] = true;   
      else firstPressInp[i] = false;
    }
  
   for(int i = 0; i < pInputs.length; i++) pInputs[i] = inputs[i];
  }
  
  void keyPressed(){ if(device == null)this.inputs = inputsKey(true, this.charinputs, this.inputs); }
  void keyReleased(){ if(device == null)this.inputs = inputsKey(false, this.charinputs, this.inputs); }
}

class InputRecord extends Structs{
  
  // "0000 (Dir) 000 (AttButs) 0 (RC) : timeactive"
  ArrayList<String> InpRec = new ArrayList<String>();
  Fighter FtoRec = null;
  int state = 0; // 0: Nothing; 1: Record; 2: Play 
  int t_sameInputs = 0;
  int timer = 0, curCollumn = 0;
  int maxActiveTime = 60*60*10, activeTime = 0;
  boolean sideSens = false;
  
  InputRecord(){
  } 
  InputRecord(Fighter FtoRec){
    this.FtoRec = FtoRec;
  }
  InputRecord(Fighter FtoRec, int state, boolean sideSens, String... RecInpData){
    this.FtoRec = FtoRec; this.state = state; this.sideSens = sideSens;
    for(String s : RecInpData){
        InpRec.add(s);
    }   
    
  } 
  
  void work(Fighter toRec){
    switch(state){
      case 0:
      break;
      case 1:
      recordInputs(toRec);
      break;
      case 2:
      playRec(toRec);
      break;
      default:
      break;
    }
  }
  
  void reset(){
    t_sameInputs = 0; timer = 0; curCollumn = 0;
  }
  void deleteRec(){
    InpRec.clear();
    reset();
    activeTime = 0;
  }
  
  void genRecData(String datnam){
    JSONObject data = new JSONObject();
    data.setInt("RecLength", InpRec.size());
    int i = 0;
    for(String Str : InpRec){
      data.setString("cRec"+i, Str);
      i++;
    }
    
    saveJSONObject(data, datnam);
  }
  
  void loadRecData(String datnam){
    JSONObject data = loadJSONObject(datnam);
    int imax = data.getInt("RecLength");
    for(int i = 0; i < imax; i++){
      InpRec.add( data.getString("cRec"+i) );

    }
  }
  
  boolean recAtEnd(){
    return curCollumn >= InpRec.size()-1;
  }
  
  void playRec(Fighter toRec){
    if(timer == 0 && curCollumn == 0){
      timer = int( InpRec.get(0).substring(toRec.inputs.length+1) );
    }
    
    for(int i = 0; i < toRec.inputs.length; i++){
      if(InpRec.get(curCollumn).charAt(i) == '1' ){

        toRec.inputs[i] = true;
        
      }
      else{toRec.inputs[i] = false;}
      
    }
    if(sideSens && toRec.dirMult == -1){
      boolean tmp = toRec.inputs[2]; toRec.inputs[2] = toRec.inputs[3]; toRec.inputs[3] = tmp;
    }
    
    timer--;
    
    if(timer <= 0 && curCollumn < InpRec.size()-1){
      curCollumn++;
      timer = int( InpRec.get(curCollumn).substring(toRec.inputs.length) );
    }
    
    
  }
  
  void recordInputs(Fighter toRec){
    if(activeTime < maxActiveTime){

    String CurRecord = "";
    for(int i = 0; i < toRec.inputs.length; i++){
      if(toRec.inputs[i]){
        CurRecord += "1";
      }
      else{ CurRecord += "0"; }
    }
    
    if(InpRec.size() == 0){
      InpRec.add(CurRecord + ":1");
      t_sameInputs = 1;
    }
    else if(InpRec.size() > 0){
      String S1 = CurRecord.substring(0, toRec.inputs.length-1),
      S2 = InpRec.get(InpRec.size()-1).substring(0, toRec.inputs.length-1);
      
      if( S1.equals( S2 ) ){
        t_sameInputs++;
        InpRec.set(InpRec.size()-1, S1 + ":" + t_sameInputs);
      }
      else{
          InpRec.add(CurRecord + ":1");
          t_sameInputs = 1;
      }
      
    }
    
  }
  
  }
  
  void printRec(){
    for(String Str : InpRec){ println(Str); }
  }
  
}

static class AnimControl extends Structs{
   private class AnimID{
    String SprsName = ""; PImage[] Sprs; int AnimSize = 0; int AnimObjCount = 0;
    AnimID(String SprsName, PImage[] Sprs, int AnimSize){
      this.SprsName = SprsName; this.Sprs = Sprs; this.AnimSize = AnimSize; this.AnimObjCount = 1;
    }
  }
    
  static private ArrayList<AnimID> AnimList = new ArrayList<AnimID>();
  
  static void enterNewAnim(Animation Anim, int Animlength){
    //removeNotUsedAnim();
    String aDatnam = Anim.datnam;
    for(AnimID a : AnimList){
      
      if( a.SprsName.equals(aDatnam) && Animlength == a.AnimSize
      ){
        //println(a.SprsName + "Anim already listed, Size: " + a.AnimSize);
        Anim.Sprites = a.Sprs; a.AnimObjCount++;
        return;
      }
      
    }  

      PImage[] SprsToPass = Anim.loadSpr_Arr(Animlength);
      AnimList.add(new AnimControl().new AnimID(aDatnam, SprsToPass, Animlength));
      Anim.Sprites = SprsToPass;
      //println(aDatnam + "Anim newly loaded");
    
  }
  
  static void removeNotUsedAnim(){
    for(int i = AnimList.size()-1; i >= 0; i--) if(AnimList.get(i).AnimObjCount <= 0) AnimList.remove(i);
  }

}

class Animation extends Structs{
  
  int curCollumn = 0;
  int timer = 0;
  
  int[] changeTimes;
  
  int X_coords, Y_coords; public float rot = 0.0;
  
  int[] sprite_ID;
  
  PImage[] Sprites; String datnam = "none";
  
  boolean loop = true;
  
  Animation(int[] changeTimes, int X_coords, int[] sprite_ID){
    this.changeTimes = changeTimes;
    this.X_coords = X_coords;
    this.sprite_ID = sprite_ID;
  }
  
    Animation(int[] changeTimes, int X_coords, int Y_coords, int[] sprite_ID){
    this.changeTimes = changeTimes;
    this.X_coords = X_coords;
    this.sprite_ID = sprite_ID;
    this.Y_coords = Y_coords;
  }
  
 Animation(int[] changeTimes, int X_coords, int Spr_Size_Arr, String datnam){
   this(changeTimes, X_coords, 0, Spr_Size_Arr, datnam);
  }
  
 Animation(int[] changeTimes, int X_coords, int Y_coords, int Spr_Size_Arr, String datnam){
    this.changeTimes = changeTimes;
    this.X_coords = X_coords;
    this.Y_coords = Y_coords;
    this.datnam = datnam;
    //println(datnam);
    //Sprites = loadSpr_Arr(Spr_Size_Arr);
     AnimControl.enterNewAnim(this, Spr_Size_Arr);
         //println("funkt");
  }
  
 Animation(int[] changeTimes, int X_coords, int Y_coords, PImage[] Sprites){
    this.changeTimes = changeTimes;
    this.X_coords = X_coords;
    this.Y_coords = Y_coords;
    this.Sprites = Sprites;
  }
  
  Animation(Animation toCopy){
    this.datnam = toCopy.datnam; //BUG: datnam standard "none" -> needed to be passed in copy constructor, otherwise bug in Animcontrol //IMPORTANT
    this.changeTimes = toCopy.changeTimes;
    this.X_coords = toCopy.X_coords;
    this.Y_coords = toCopy.Y_coords;
    this.Sprites = toCopy.Sprites;
    this.loop = toCopy.loop;
    AnimControl.enterNewAnim(toCopy, toCopy.changeTimes.length);
  }
  
  /*void finalize(){
    for(AnimControl.AnimID a : AnimControl.AnimList){
      if( a.Sprs == Sprites && a.AnimSize == changeTimes.length ){
        a.AnimObjCount--; println(a.SprsName+a.AnimObjCount); break;
      }
        }
  }*/
  
  void handleAnim(){
    boolean incrTimer = true;
    if(curCollumn == 0 && timer <= 0){
      
    }
    else if( curCollumn < Sprites.length-1 && timer >= changeTimes[curCollumn]){
      curCollumn++;
      timer = 0;
    }
    else if( curCollumn >= Sprites.length-1 && timer >= changeTimes[curCollumn] && loop){
      curCollumn = 0;
      timer = 0;
      incrTimer = false;
    }
    
    if(incrTimer){
      timer++; //reaching integer limit if loop == false ?
    }
  }
  
  void draw(float l_x, float l_y){
    pushMatrix();
    translate(l_x, l_y);
      rotate(rot);
      image( Sprites[curCollumn],
      X_coords, 
      Y_coords);   
    popMatrix();
  }
  
  void Reset(){
    timer = 0;
    curCollumn = 0;
  }
  
  PImage[] loadSpr_Arr(int Spr_Size_Arr){
      PImage[] SprsToRet = new PImage[Spr_Size_Arr];
    for(int i = 0; i < Spr_Size_Arr; i++){
      println(datnam + i + ".png");
      SprsToRet[i] = loadImage(datnam + i + ".png");
    }
    
    return SprsToRet;
  }
  
  
}

class Projectile extends Structs{
  final int MID = 0;
  final int HIGH = 1;
  final int LOW = 2;
  
  int addx = 0, addy = 0;
  float x, y;
  int br = 0, ho = 0;
  int dirMult = 1;
  
  float forcex, forcey;
  
  float m = 1.2;
  
  int attKind = 3;
  int exTimer = 200;
  int hitStun = 10;
  int blockStun = 5;
  int damage = 10;
  
  boolean effByFric = false;
  boolean effByGrav = false;
  boolean destroyedByCol = false;
  boolean destroyedByHit = true, hitOpp = true;
  
  PImage Spr;
  Animation Anim = null;
  Animation destrEff = null;
  
  ColCircle HitBox;
  
  Projectile(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
    this.x = x; this.y = y; this.br = br; this.ho = ho; this.forcex = forcex; this.forcey = forcey;
    this.m = m;this.exTimer = exTimer;this.hitStun = hitStun; this.blockStun = blockStun; this.damage = damage;
    this.effByFric = effByFric; this.effByGrav = effByGrav; this.destroyedByCol = destroyedByCol;
    this.HitBox = new ColCircle(0, 0, br, ho, forcex, forcey, exTimer);
        HitBox.x = x;
    HitBox.y = y;
    Spr = Proj_sprs[0];
    
  }
  
    Projectile(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit){
      this(x, y, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol); this.destroyedByHit = destroyedByHit;
    }
    Projectile(float x, float y, int addx, int addy, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, 
    boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit, boolean hitOpp){
      this(x, y, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit); this.hitOpp = hitOpp; this.addx = addx; this.addy = addy;
    }
    Projectile(float x, float y, int addx, int addy, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, 
    boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit, boolean hitOpp, int dirMult){
      this(x, y, addx, addy, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit, hitOpp);  this.dirMult = dirMult;
    }
    Projectile(Projectile P){
      this(P.x, P.y, P.addx, P.addy, P.br, P.ho, P.forcex, P.forcey, P.m, P.exTimer, P.hitStun, P.blockStun, P.damage, 
      P.effByFric, P.effByGrav, P.destroyedByCol, P.destroyedByHit, P.hitOpp, P.dirMult);
    }
    
 Projectile copy(){return new Projectile(this);} //needs to be implemented in unterklassen to use overwritten function
 void setXY(int mult, float x, float y){this.x = x + addx*mult;this.forcex *= mult; this.y = y + addy; this.dirMult = mult; this.HitBox.x = this.x; this.HitBox.y = this.y;};
 void setAnims(Animation a, Animation d){ if( a != null && d != null) Anim = new Animation(a); destrEff = new Animation(d);};
  
 void specialStuff(Fighter Pl, Fighter Opp){
   //println("Oberklasse bbb");
 }
 
 void specialOnHit(Fighter Pl, Fighter Opp){
 }
  
 void gameLogic(Fighter Pl, Fighter Opp){
    if(Anim != null){
      Anim.handleAnim();
    }
    if(effByGrav){
      forcey += m * 0.1;
    }
    if(effByFric){
      if(forcex < 0){
        forcex += 0.02;
      }
      else if(forcex > 0){
        forcex -= 0.02;
      }
    }
    if(!(-0.05 < forcex && forcex < 0.05)){  
    x += forcex;//*dirMult;
    }
    y += forcey;

    if(destroyedByCol && y > GROUNDHEIGHT){
      exTimer = 0;
    }
    else if(y > GROUNDHEIGHT){
      y =  GROUNDHEIGHT;
      forcey = 0;
    }
    
    HitBox.x = x;
    HitBox.y = y;
    HitBox.forcex = forcex;
    HitBox.forcey = forcey;
    
    if(exTimer > 0){
      exTimer--;
    }
    
    specialStuff(Pl, Opp);
  }
  
  void draw(){
    pushMatrix();
    translate( x, y);

    scale( dirMult, 1);
    imageMode(CENTER);
    if(Anim != null){
       image( Anim.Sprites[Anim.curCollumn], Anim.X_coords, Anim.Y_coords);
    }
    else{
      image(Spr, 0, 0);
    }
    popMatrix();
            fill(255, 0, 0);
      HitBox.draw(1);
        
  }
  
}

class WH_Oilpuddle extends Projectile{
  WH_Oilpuddle(float x, float y, int addx, int addy, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, 
    boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit, boolean hitOpp){
      super(x, y, addx, addy, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit, hitOpp);
    }
    WH_Oilpuddle(Projectile P){
      super(P.x, P.y, P.addx, P.addy, P.br, P.ho, P.forcex, P.forcey, P.m, P.exTimer, P.hitStun, P.blockStun, P.damage, P.effByFric, P.effByGrav, P.destroyedByCol, P.destroyedByHit, P.hitOpp);}
      
    Projectile copy(){return new WH_Oilpuddle(this);}

    void specialStuff(Fighter Pl, Fighter Opp){   
      checkInOil(Pl); checkInOil(Opp);

    }
    
    void checkInOil(Fighter F){
      //ColRect Fc = F.CollisionBox;
      if( recPointCol(F.x, F.y, this.x-br/2, this.y, this.br, this.ho) && new Grounded().cond(F, F) ){
        F.Force.x += F.Force.x * 0.08;  
      }

    }
    
}

class PH_Apple extends Projectile{
  F_PHaus F;
  
    PH_Apple(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, 
    boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit, F_PHaus F){
      super(x, y, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit);
      this.F = F;
    }
  
   void specialStuff(Fighter Pl, Fighter Opp){
     println("booohh");
     for(ColCircle c : F.HurtBoxes){
       if(c.compare(HitBox, F.dirMult, 1)){
         F.curFartMeter += 30;
         exTimer = 0;
       }
     }
 }
}

class FH_Proj1 extends Projectile{
  Animation AnimMid, AnimUp, AnimDown;
  
  FH_Proj1(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, 
  boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit, Animation AnimMid, Animation AnimUp, Animation AnimDown){
    super(x, y, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit);
    this.AnimMid = AnimMid;
    this.AnimUp = AnimUp; 
    this.AnimDown = AnimDown;
  }
  
    void draw(){
        //fill(255, 0, 0);
        //HitBox.draw();
    
    pushMatrix();
    translate( x, y - Anim.Sprites[Anim.curCollumn].height/2);
    int facing = 1;
    if(forcex < 0){
      facing = -1;
    }
    scale( facing, 1);
    imageMode(CENTER);
    if(Anim != null){
       image( Anim.Sprites[Anim.curCollumn], 0, 0);
    }
    else{
      image(Spr, 0, 0);
    }
    popMatrix();
    
  }
  
  void specialStuff(Fighter Pl, Fighter Opp){
    if(y < GROUNDHEIGHT){
      attKind = 1;
    }
    else if(y >= GROUNDHEIGHT){
      attKind = 2;
    }
    
    if(forcey < -2.6){
      Anim = AnimUp;
    }
    else if(forcey > 2.6){
      Anim = AnimDown;
    }
    else{
      Anim = AnimMid;
    }
    
  }
  
}

class SelfHitProj extends Projectile{
  
    SelfHitProj(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
    super(x, y, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol);
    Spr = Proj_sprs[0];
  }
  
 void specialStuff(Fighter Pl, Fighter Opp){
    for(int i = Pl.HurtBoxes.size()-1; i >= 0; i--){
      if(Pl.HurtBoxes.get(i).compare(this.HitBox, 1, 1)){
        Pl.curHP -= damage; 
        Pl.Force.x = forcex; 
        Pl.Force.y = forcey;
        exTimer = 0;
        
      }
    }
  }
  
}

class BoomerangProj extends Projectile{
  
  BoomerangProj(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
    super(x, y, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol);
    Spr = Proj_sprs[1];
  }
  
  void specialStuff(Fighter Pl, Fighter Opp){
    if(x-40 < Camerabox.x - Camerabox.br/2){
      x = Camerabox.x - Camerabox.br/2 +40;
      forcex *= -1;
    }
    else if(x+40 > Camerabox.x + Camerabox.br/2){
      x = Camerabox.x + Camerabox.br/2 -40;
      forcex *= -1;
    }
  
  }
  
   void specialOnHit(Fighter Pl, Fighter Opp){
     forcex *= -1;
 }

}

class MatteProj extends Projectile{
  float br2 = 0, ho2 = 0;
  
    MatteProj(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, boolean effByFric, boolean effByGrav, boolean destroyedByCol, float br2, float ho2){
    super(x, y, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol);
    this.br2 = br2;
    this.ho2 = ho2;
    Spr = Proj_sprs[1];
  }
  
  void specialStuff(Fighter Pl, Fighter Opp){
    if(recPointCol(Opp.x, Opp.y, HitBox.x, HitBox.y, br2, ho2) && frame % 10 == 0 ){
      Opp.curHP--;
      Opp.curSuper--;
      Pl.curHP++;
      Pl.curSuper++;
      println("boooi");
    }
    
    fill(40, 200, 0, 80);
    rect(x - br2/2, y-ho2, br2, ho2);
  }
  
}

abstract class TimedScreenEff extends Structs{
    float x, y = 0;
  int exTimer = 30;
  
  void draw(){
        if(exTimer > 0) exTimer--;
  }

}

class VisualEffect extends TimedScreenEff{
  float scale = 0; int xdirMult = 1;
  Animation Anim;
  
  
  VisualEffect(float x, float y, Animation Anim, float scale){
    this.Anim = new Animation(Anim);
    this.Anim.Reset();
    this.x = x;
    this.y = y;
    this.scale = scale;
    this.exTimer = sumOfArr(Anim.changeTimes);
  }
  VisualEffect(float x, float y, Animation Anim, float scale, int xdirMult){
    this(x, y, Anim, scale); this.xdirMult = xdirMult;
  }
  
  void draw(){
    Anim.handleAnim();
    pushMatrix();
    translate(x,y);
    imageMode(CENTER);
    if(scale > 1){
    scale((scale*xdirMult+1)*0.6, scale+1);
    }
    image(Anim.Sprites[Anim.curCollumn], 0, 0);
    popMatrix();
    
    super.draw();
    
  }
  
}

class PopUpMssg extends TimedScreenEff{
  String Text = "POP-UP"; int textSize = 20; color textF = 0;
  PopUpMssg(float x, float y, String Text, int textSize, color textF){
    this.x = x; this.y = y;
    this.Text = Text; this.textSize = textSize; this.textF = textF;
  }
  void draw(){
    textSize(textSize);
    fill(textF);
    textMode(CENTER);
    text(Text, x, y);
    super.draw();
  }
  
}

class GridCursor<T extends MenuBox> extends Structs{
  T[][] Grid;
  int xGrid = 0, yGrid = 0, clickBut = 0, backBut = 0;
  PlControl Con; boolean clicked = false, backClick = false;
  
  GridCursor(T[][] Grid, int xGrid, int yGrid, PlControl Con, int clickBut, int backBut){
    this.Grid = Grid; this.xGrid = xGrid; this.yGrid = yGrid; 
    this.Con = Con; this.clickBut = clickBut; this.backBut = backBut;
  }
  
  void draw(int lx, int ly){
    T m = Grid[yGrid][xGrid];
    fill(0, 0, 0, 0);
    ellipse(m.x, m.y, 100, 100);
  }
  
  T CurMB(){return Grid[yGrid][xGrid];}
  
  void Logic(){
    if(Con.firstPressInp[0] && Con.inputs[0] && !checkOutRange(xGrid, yGrid-1)) yGrid-=1;
    if(Con.firstPressInp[1] && Con.inputs[1] && !checkOutRange(xGrid, yGrid+1)) yGrid+=1;
    if(Con.firstPressInp[2] && Con.inputs[2] && !checkOutRange(xGrid+1, yGrid)) xGrid+=1;
    if(Con.firstPressInp[3] && Con.inputs[3] && !checkOutRange(xGrid-1, yGrid)) xGrid-=1;
    if(Con.firstPressInp[clickBut] && Con.inputs[clickBut]){Grid[yGrid][xGrid].clicked = true; clicked = true;}
    else clicked = false;
    if(Con.firstPressInp[backBut] && Con.inputs[backBut]){backClick = true;}
    else backClick = false;
  }
  
  boolean checkOutRange(int lx, int ly){
    if(lx < 0 || ly < 0 || lx >= Grid[yGrid].length || ly >= Grid.length) return true;
    if(ly > yGrid && lx < Grid[yGrid].length){ if(lx > Grid[yGrid+1].length-1) return true; }
    if(ly < yGrid && lx < Grid[yGrid].length){ if(lx > Grid[yGrid-1].length-1) return true; }
    if(Grid[ly][lx] == null) return true;
    return false;
  }
  
}

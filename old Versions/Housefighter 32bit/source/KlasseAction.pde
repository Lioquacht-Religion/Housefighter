class Action{
  final int NOTHING = 0;
  final int HIGH = 1;
  final int LOW = 2;
  final int MID = 3;
  final int AIR = 4;
  int attKind = NOTHING;
  
  int curMoveDur = 0;
  int curCollumn = 0;
  int affHitStunT = 10;
  int affBlockStunT = 5;
  int damage = 10;
  float gravMult = 1.;
  
  boolean gravityActive = true;
  boolean addingForce = true;
  boolean knocksDown = false;
  boolean multiHit = false;
  boolean hitCancel = true;
  
  boolean firstHit = true;
  boolean resetAnim = false;
  
  
  // x-Force to set to
  // y-Force to set to
  // durtime to set x-, y-Force
  float[][] setForceAtDur = new float[1][2];
  //int[] whenToUpdForce;
  
  ArrayList<ArrayList<ColCircle>> HitBoxCollect = new ArrayList<ArrayList<ColCircle>>();
  
  ArrayList<ArrayList<ColCircle>> HurtBoxCollect = new ArrayList<ArrayList<ColCircle>>();

  int[] whenToUpdBoxs = new int[1];
  
  int[] sprsIds = new int[1];
  
  String datnam = "No File";
  
  Animation AttAnim = null;
  
  Action(){
    HitBoxCollect.add(new ArrayList<ColCircle>());
    HurtBoxCollect.add(new ArrayList<ColCircle>());
  }
  
    Action(int[] sprsIds){
      this.sprsIds = sprsIds;
    HitBoxCollect.add(new ArrayList<ColCircle>());
    HurtBoxCollect.add(new ArrayList<ColCircle>());
  }
  
    Action(Animation AttAnim, boolean resetAnim){
      this.AttAnim = AttAnim;
      this.resetAnim = resetAnim;
    HitBoxCollect.add(new ArrayList<ColCircle>());
    HurtBoxCollect.add(new ArrayList<ColCircle>());
  }
  
   Action(String datnam, Animation AttAnim, boolean resetAnim){
      Action a = loadActionData(datnam);
      copy(a);
      this.AttAnim = AttAnim;
      this.resetAnim = resetAnim;
  }
  
    Action(int[] sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit){
      this.sprsIds = sprsIds;
      this.affHitStunT = affHitStunT;
      this.affBlockStunT = affBlockStunT;
      this.damage = damage;
      this.attKind = attKind;
      this.gravityActive = gravityActive;
      this.addingForce = addingForce;
      this.knocksDown = knocksDown;
      this.multiHit = multiHit;
      HitBoxCollect.add(new ArrayList<ColCircle>());
      HurtBoxCollect.add(new ArrayList<ColCircle>());
  }
  
    Action(Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit){
      this.AttAnim = AttAnim;
      this.affHitStunT = affHitStunT;
      this.affBlockStunT = affBlockStunT;
      this.damage = damage;
      this.attKind = attKind;
      this.gravityActive = gravityActive;
      this.addingForce = addingForce;
      this.knocksDown = knocksDown;
      this.multiHit = multiHit;
      HitBoxCollect.add(new ArrayList<ColCircle>());
      HurtBoxCollect.add(new ArrayList<ColCircle>());
  }
  
  //Copy-Konstruktor // Arraykopieren
  Action(Action a){
    this.sprsIds = a.sprsIds;
    this.affHitStunT = a.affHitStunT;
    this.affBlockStunT = a.affBlockStunT;
    this.attKind = a.attKind;
    this.gravityActive = a.gravityActive;
    this.addingForce = a.addingForce;
    this.knocksDown = a.knocksDown;
    this.multiHit = a.multiHit;
    copy(a);
  }
  
  //JSON-Datei-Konstruktor
    Action(String datnam, int[] sprsIds, int affHitStunT,int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit){
      Action a = loadActionData(datnam);
      copy(a);
    this.sprsIds = sprsIds;
    this.affHitStunT = affHitStunT;
    this.affBlockStunT = affBlockStunT;
    this.damage = damage;
    this.attKind = attKind;
    this.gravityActive = gravityActive;
    this.addingForce = addingForce;
    this.knocksDown = knocksDown;
    this.multiHit = multiHit;
    this.datnam = datnam;
  }
  
   Action(String datnam, Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit){
      Action a = loadActionData(datnam);
      copy(a);
    this.AttAnim = AttAnim;
    this.affHitStunT = affHitStunT;
    this.affBlockStunT = affBlockStunT;
    this.damage = damage;
    this.attKind = attKind;
    this.gravityActive = gravityActive;
    this.addingForce = addingForce;
    this.knocksDown = knocksDown;
    this.multiHit = multiHit;
    this.datnam = datnam;
  }
  
  void playAction2(Fighter Pl, Fighter Opp, Action ToSetTo){
    boolean incrDur = true;
    
    if(curCollumn == 0 && curMoveDur == 0){
      
      if(AttAnim != null){
        Pl.CurAnimation = AttAnim;
        if(resetAnim){
          AttAnim.timer = 0; AttAnim.curCollumn = 0;
        }
      }
      else{ Pl.CurAnimation = null;}
      
    clearAndCurBoxes(Pl);  
    
    if(addingForce){
      Pl.Force.x += setForceAtDur[curCollumn][0] * Pl.dirMult;
      Pl.Force.y += setForceAtDur[curCollumn][1];
    }
    else {
      Pl.Force.x = setForceAtDur[curCollumn][0] * Pl.dirMult;
      Pl.Force.y = setForceAtDur[curCollumn][1];
    }
    specialEffect(Pl, Opp);
    }
    //#####
    else
    if(curMoveDur >= whenToUpdBoxs[curCollumn] && curCollumn < HitBoxCollect.size()-1){
      curCollumn++;
      curMoveDur = 0;
    clearAndCurBoxes(Pl);
    
        if(addingForce){
      Pl.Force.x += setForceAtDur[curCollumn][0] * Pl.dirMult;
      Pl.Force.y += setForceAtDur[curCollumn][1];
    }
    else {
      Pl.Force.x = setForceAtDur[curCollumn][0] * Pl.dirMult;
      Pl.Force.y = setForceAtDur[curCollumn][1];
    }
      specialEffect(Pl, Opp);
      

    }
    //#####
    else if(curCollumn >= HitBoxCollect.size()-1 && curMoveDur >= whenToUpdBoxs[HitBoxCollect.size()-1]){    
      curCollumn = 0;
      curMoveDur = 0;
      Pl.CurAction.reset();
      Pl.CurAction = ToSetTo;
      clearAndCurBoxes(Pl);
      incrDur = false;
      specialEffectOnEnd(Pl, Opp);
    }
    
    alwaysSpecialEffect(Pl, Opp);
    
    if(incrDur){
    curMoveDur++;
    }
  }
  
  void clearAndCurBoxes(Fighter Pl){
                Pl.HitBoxes.clear();
                Pl.HurtBoxes.clear();
       for(int i = 0; i < HitBoxCollect.get(curCollumn).size(); i++){
           Pl.HitBoxes.add(HitBoxCollect.get(curCollumn).get(i));
         }
         
       for(int i = 0; i < HurtBoxCollect.get(curCollumn).size(); i++){
           Pl.HurtBoxes.add(HurtBoxCollect.get(curCollumn).get(i));
         }
 /*     for(int i = 0; i < HitBoxCollect.get(curCollumn).size(); i++){
         Pl.HitBoxes.add(new ColCircle(HitBoxCollect.get(curCollumn).get(i)));
         Pl.HitBoxes.get(i).addx *= Pl.dirMult;
         Pl.HitBoxes.get(i).forcex *= Pl.dirMult;
    }
   
      for(int i = 0; i < HurtBoxCollect.get(curCollumn).size(); i++){
         Pl.HurtBoxes.add(new ColCircle(HurtBoxCollect.get(curCollumn).get(i)));
         Pl.HurtBoxes.get(i).addx *= Pl.dirMult;
         Pl.HurtBoxes.get(i).forcex *= Pl.dirMult;
    }*/
  }
  
  void changeXDirOfBoxes(){
        for(ArrayList<ColCircle> arrc : HitBoxCollect){
          for(ColCircle c : arrc){
        c.addx *= -1;
        c.forcex *= -1;
          }
      }
              for(ArrayList<ColCircle> arrc : HurtBoxCollect){
          for(ColCircle c : arrc){
        c.addx *= -1;
        c.forcex *= -1;
          }
      }
      
  }
  
  void changeXDirOfForces(){
    for(int i = 0; i < setForceAtDur.length; i++){
      setForceAtDur[i][0] *= -1;
    }
  }
  
  
  
  void reset(){
      curMoveDur = 0;
      curCollumn = 0;
      firstHit = true;
  }
  
  
  void updFrameDataArr(int index, int value){
          int[] l_frameData = new int[HitBoxCollect.size()];
      for(int i = 0; i < whenToUpdBoxs.length; i++){
        l_frameData[i] = whenToUpdBoxs[i];
      }
      whenToUpdBoxs = l_frameData;
      
      whenToUpdBoxs[index] = value;
  }
  
    void updFrameDataArr_float(int index, float fx, float fy){
          float[][] l_forceData = new float[HitBoxCollect.size()][2];
      for(int i = 0; i < setForceAtDur.length; i++){
        for(int j = 0; j < 2; j++){
          l_forceData[i][j] = setForceAtDur[i][j];
        }
      }
      setForceAtDur = l_forceData;
      
      setForceAtDur[index][0] = fx;
      setForceAtDur[index][1] = fy;
  }
  
  int sumOfFrameArr(int index){
    int sum = 0;
    for(int i = 0 ; i < index; i++){
      sum += whenToUpdBoxs[i];
    }
    
    return sum;
  }
  
  void copy(Action a){
      for(int i = 0; i < a.HitBoxCollect.size(); i++){
      HitBoxCollect.add(new ArrayList<ColCircle>());
      for(int j = 0; j < a.HitBoxCollect.get(i).size(); j++){
        HitBoxCollect.get(i).add(new ColCircle(a.HitBoxCollect.get(i).get(j)));
      }
    }
    for(int i = 0; i < a.HitBoxCollect.size(); i++){
      HurtBoxCollect.add(new ArrayList<ColCircle>());
      for(int j = 0; j < a.HurtBoxCollect.get(i).size(); j++){
        HurtBoxCollect.get(i).add(new ColCircle(a.HurtBoxCollect.get(i).get(j)));
      }
    }
   setForceAtDur = new float[a.setForceAtDur.length][2];
   for(int i = 0; i < a.setForceAtDur.length; i++){
     setForceAtDur[i][0] = a.setForceAtDur[i][0];
     setForceAtDur[i][1] = a.setForceAtDur[i][1];
   }
   whenToUpdBoxs = new int[a.whenToUpdBoxs.length];
   for(int i = 0; i < a.whenToUpdBoxs.length; i++){
     whenToUpdBoxs[i] = a.whenToUpdBoxs[i]; 
   }
  }
  
  void addAllLists(int index, int frameData, float fx, float fy){
         HitBoxCollect.add(new ArrayList<ColCircle>());
    HurtBoxCollect.add(new ArrayList<ColCircle>());
      updFrameDataArr(index, frameData);
    updFrameDataArr_float(index, fx, fy);
  }
  
  void alwaysSpecialEffect(Fighter Pl, Fighter Opp){
  }
  
  void specialEffect(Fighter Pl, Fighter Opp){
  }
  
  void specialEffectOnHit(Fighter Pl, Fighter Opp){
  }
  
  void specialEffectOnEnd(Fighter Pl, Fighter Opp){
  }
  
}

class extraForceOnHitAct extends Action{
  float extrFx = 0;
  float extrFy = 0;
  
     extraForceOnHitAct(String datnam, Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
       float extrFx, float extrFy){
       super(datnam, AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
       this.extrFx = extrFx;
       this.extrFy = extrFy;
     }
  
    void specialEffectOnHit(Fighter Pl, Fighter Opp){
      Pl.CurAction.reset();
      Pl.Force.x += extrFx;
      Pl.Force.y += extrFy;
      Pl.gforce = 0;
  }
  
}

class ChangeOnEndAct extends Action{
  Action ActToChangeTo;
  
   ChangeOnEndAct(Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo;
  }
  
   ChangeOnEndAct(String datnam, Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(datnam, AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo;
  }
  
    void specialEffectOnEnd(Fighter Pl, Fighter Opp){
        Pl.CurAction.reset();
        Pl.changeAction(ActToChangeTo);
  }
  
}

class ChangeOnCondAct extends ChangeOnEndAct{
  
   ChangeOnCondAct(Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit, ActToChangeTo);
        this.ActToChangeTo = ActToChangeTo;
  }
  
   ChangeOnCondAct(String datnam, Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(datnam, AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit, ActToChangeTo);
  }
    //void specialEffectOnEnd(Fighter Pl, Fighter Opp){}
  
    void alwaysSpecialEffect(Fighter Pl, Fighter Opp){
      if(Pl.y >= GROUNDHEIGHT || Pl.CollisionBox.bottom){
      Pl.CurAction.reset();
      Pl.CurAction = ActToChangeTo;
      clearAndCurBoxes(Pl);
      curMoveDur = -1;
      }
  }
  
}

class HoldButToKeepAct extends Action{  
    Action ActToChangeTo;
    int holdTimer = 0;
  
   HoldButToKeepAct(Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo;
  }
  
   HoldButToKeepAct(String datnam, Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(datnam, AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo;
  }
  
     void alwaysSpecialEffect(Fighter Pl, Fighter Opp){
             if(Pl.inputs[6] && holdTimer <= 60){
          this.curCollumn = 0;
      }
      else{
        holdTimer = 0;
        Pl.changeAction(ActToChangeTo);
        println("ButChange");
      }
      
      if(Pl.inputs[2]){
        Pl.Force.x = 4;
      }
      if(Pl.inputs[3]){
        Pl.Force.x = -4;
      }
      
      holdTimer++;
  }
  
     void specialEffectOnEnd(Fighter Pl, Fighter Opp){
        Pl.changeAction(ActToChangeTo);
  }
  
}

class ChangeAction extends Action{
  Action ActToChangeTo;
  
        ChangeAction(int[] sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo;
  }
            ChangeAction(Animation sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo;
  }
          ChangeAction(String datnam, Animation sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(datnam, sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo;
  }
  
    void specialEffectOnHit(Fighter Pl, Fighter Opp){
      Pl.CurAction.reset();
      reset();
      curMoveDur = -1;
      ActToChangeTo.reset();
      Pl.CurAction = ActToChangeTo;
      Pl.CurAction.reset();
      println("CHANGE");
      
  }
  
}

class GrabAction extends Action{
  float[][] grabOppPos = new float[1][2];
  
      GrabAction(int[] sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, float[][] grabOppPos){
        super(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, multiHit, knocksDown);
        this.grabOppPos = grabOppPos;
  }
            GrabAction( Animation sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, float[][] grabOppPos){
        super( sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.grabOppPos = grabOppPos;
  }
  
  void specialEffect(Fighter Pl, Fighter Opp){
    //if(Opp.CurAction == Opp.Blocking || Opp.CurAction == Opp.Standing || Opp.CurAction == Opp.Crouching ||Opp.CurAction == Opp.BeingGrapped){
    Opp.x = Pl.x + grabOppPos[curCollumn][0] * Pl.dirMult;
    Opp.y = Pl.y + grabOppPos[curCollumn][1];
    //HitBoxCollect.get(curCollumn).clear();
    //HitBoxCollect.get(curCollumn).add(new ColCircle((Pl.x*Pl.dirMult + Opp.x*Opp.dirMult)*-1 , -Pl.y + Opp.y, 250, 250, 0, 0, 1));
    Opp.curHP -= damage;
    Opp.CurAction.reset();
    Opp.BeingGrapped.whenToUpdBoxs[0] = whenToUpdBoxs[curCollumn]+10;
    Opp.CurAction = Opp.BeingGrapped;
    Opp.CurAction.reset();
    Opp.BeingGrapped.reset();

    //}
  }
  
  void specialEffectOnHit(Fighter Pl, Fighter Opp){
    //Opp.CurAction.reset();
    Opp.CurAction = Opp.BeingGrapped;
       //Ok, Ich bin dumm, also echt, nur um den Damage auszugleichen // funktioniert eh nicht, too BAD
  
  }
  
  void specialEffectOnEnd(Fighter Pl, Fighter Opp){
        Opp.Force.x += 10*-Pl.dirMult;
        Opp.Force.y += -30;
        Opp.InHitStun.whenToUpdBoxs[0] = affHitStunT;
        Opp.changeAction(Opp.InHitStun);      curCollumn = 0;
      curMoveDur = 0;
     /* Pl.CurAction.reset();
      Pl.CurAction = Pl.Standing;
      clearAndCurBoxes(Pl);
        curMoveDur = -1;*/
  }
  
}

class ProjAction extends Action{
  float m = 1;
  int exTimer = 200; 
  boolean effByFric, effByGrav, destroyedByCol = false, destroyedByHit = true;
  float fx, fy = 0;
  
  public Animation ProjAnim = null;
  public Animation destrEffAnim = null;
  
  
      ProjAction(int[] sprsIds){
      this.sprsIds = sprsIds;
    HitBoxCollect.add(new ArrayList<ColCircle>());
    HurtBoxCollect.add(new ArrayList<ColCircle>());
  }
  
        ProjAction(int[] sprsIds, int affHitStunT){
      this.sprsIds = sprsIds;
      this.affHitStunT = affHitStunT;
      HitBoxCollect.add(new ArrayList<ColCircle>());
      HurtBoxCollect.add(new ArrayList<ColCircle>());
  }
  
       ProjAction(int[] sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
     float m, int exTimer, float fx, float fy, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
      super(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
      this.fx = fx;
      this.fy = fy;
      this.m = m;
      this.exTimer = exTimer; 
      this.effByFric = effByFric; 
      this.effByGrav = effByGrav;
      this.destroyedByCol = destroyedByCol;
      HitBoxCollect.add(new ArrayList<ColCircle>());
      HurtBoxCollect.add(new ArrayList<ColCircle>());
  }
  
     ProjAction(int[] sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
     float m, int exTimer, float fx, float fy, boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit){
      super(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
      this.fx = fx;
      this.fy = fy;
      this.m = m;
      this.exTimer = exTimer; 
      this.effByFric = effByFric; 
      this.effByGrav = effByGrav;
      this.destroyedByCol = destroyedByCol;
      this.destroyedByHit = destroyedByHit;
      HitBoxCollect.add(new ArrayList<ColCircle>());
      HurtBoxCollect.add(new ArrayList<ColCircle>());
  }
  
  ProjAction(Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
  float m, int exTimer, float fx, float fy, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
    super(AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
          this.fx = fx;
      this.fy = fy;
      this.m = m;
      this.exTimer = exTimer; 
      this.effByFric = effByFric; 
      this.effByGrav = effByGrav;
      this.destroyedByCol = destroyedByCol;
  }
  
  void specialEffect(Fighter Pl, Fighter Opp){
    if(curCollumn == 1){
    Pl.Projectiles.add(new Projectile(Pl.x + 100*Pl.dirMult, Pl.y-200, 80, 80, Pl.dirMult * fx , fy, m, exTimer, affHitStunT, affBlockStunT, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit));
  }
  
  }
  
}

class FHaus_ProjAction extends ProjAction{
  Animation AnimUp, AnimMid, AnimDown;
  
  FHaus_ProjAction(Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
  float m, int exTimer, float fx, float fy, boolean effByFric, boolean effByGrav, boolean destroyedByCol, Animation AnimUp, Animation AnimMid, Animation AnimDown){
    super(AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit, m, exTimer, fx, fy, effByFric, effByGrav, destroyedByCol);
        this.AnimMid = AnimMid;
    this.AnimUp = AnimUp; 
    this.AnimDown = AnimDown;
    
  }
  
    void specialEffect(Fighter Pl, Fighter Opp){
    if(curCollumn == 1){
      FH_Proj1 p = new FH_Proj1(Pl.x + 100*Pl.dirMult, Pl.y-120, 80, 80, Pl.dirMult * fx , fy, m, exTimer, affHitStunT, affBlockStunT, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit,
    AnimUp, AnimMid, AnimDown);
      p.Anim = new Animation(ProjAnim);
      p.AnimUp = new Animation(AnimUp);
      p.AnimMid = new Animation(AnimMid);
      p.AnimDown= new Animation(AnimDown);
      p.destrEff = new Animation(destrEffAnim); 
    Pl.Projectiles.add(p);
  }
  
}
}

class HHaus_ProjAction1 extends ProjAction{
  
  
  HHaus_ProjAction1(int[] sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
     float m, int exTimer, float fx, float fy, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
    super(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit, m, exTimer, fx, fy, effByFric, effByGrav, destroyedByCol);
  }
  
  HHaus_ProjAction1(Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
  float m, int exTimer, float fx, float fy, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
    super(AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit, m, exTimer, fx, fy, effByFric, effByGrav, destroyedByCol);
  }
 
    void specialEffect(Fighter Pl, Fighter Opp){
    if(curCollumn == 1){
      
      Projectile p = new SelfHitProj(Camerabox.x + Camerabox.br/2*Pl.dirMult, initHeight/2-100, 80, 80, Pl.dirMult * fx , fy, m, exTimer, affHitStunT, affBlockStunT, damage, effByFric, effByGrav, destroyedByCol);
      p.Anim = new Animation(ProjAnim);
      p.destrEff = new Animation(destrEffAnim); 
      
    Pl.Projectiles.add(p);
  } 
  }

}

class HHaus_ProjAction2 extends ProjAction{
  
    HHaus_ProjAction2(int[] sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
     float m, int exTimer, float fx, float fy, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
    super(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit, m, exTimer, fx, fy, effByFric, effByGrav, destroyedByCol);
  }
  
  HHaus_ProjAction2(int[] sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
     float m, int exTimer, float fx, float fy, boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit){
    super(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit, m, exTimer, fx, fy, effByFric, effByGrav, destroyedByCol, destroyedByHit);
  }
 
    void specialEffect(Fighter Pl, Fighter Opp){
    if(curCollumn == 1){
    Pl.Projectiles.add(new BoomerangProj(Pl.x + 200 * Pl.dirMult, Pl.y-180, 80, 80, Pl.dirMult * fx , fy, m, exTimer, affHitStunT, affBlockStunT, damage, effByFric, effByGrav, destroyedByCol));
  } 
  }

}

class HHaus_ProjAction3 extends ProjAction{
  
  HHaus_ProjAction3(int[] sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
     float m, int exTimer, float fx, float fy, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
       super(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit, m, exTimer, fx, fy, effByFric, effByGrav, destroyedByCol);
  }
  
      void specialEffect(Fighter Pl, Fighter Opp){
    if(curCollumn == 1){
    Pl.Projectiles.add(new MatteProj(Pl.x + 200 * Pl.dirMult, Pl.y-180, 0, 0, Pl.dirMult * fx , fy, m, exTimer, affHitStunT, affBlockStunT, damage, effByFric, effByGrav, destroyedByCol, 300, 100));
  } 
  }
  
}

class Action{
  final static int NOTHING = 0, HIGH = 1, LOW = 2, MID = 3, AIR = 4, GRAB = 5;
  final static int NORMAL = 0, SPECIAL = 1, SUPER = 2,  HITSTATE = 100;
  final static int WALLSTICK = 0, WALLBOUNCE = 1, JUGGLE = 2, GROUNDBOUNCE = 3, STAGGER = 4; //Opp.WallStick, Opp.WallBounce, Opp.Juggle, Opp.GroundBounce, Opp.Stagger
  int attKind = NOTHING;
  int attWeight = 0, attRank = 0; //100 = hitstate, 0 = Normal, 1 = Special, 2 = Super 
  
  int curMoveDur = 0, curCollumn = 0;
  int affHitStunT = 10, affBlockStunT = 5;
  int damage = 0, superNeed = 0;
  float gravMult = 1., fricMult = 1., pushBackMult = 1;
  
  boolean gravityActive = true, addingForce = true, knocksDown = false, multiHit = false;
  boolean hitCancel = true, selfCancel = false, jumpCancel = false, dashCancel = false, allButSelfCancel = false;
  boolean collision = true;
  
  boolean firstHit = true;
  boolean resetAnim = false;
  
  ActEffect[] ActEffs = {};
  Condition[] Conds = {};
  // x-Force to set to
  // y-Force to set to
  // durtime to set x-, y-Force
  float[][] setForceAtDur = new float[1][2];
  
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
      Action(int[] sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, int attRank, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit){
         this(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit); this.attRank = attRank;
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
  Action(Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, int attRank, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit){
    this(AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit); this.attRank = attRank;  
  }
      Action(String datnam, Animation AttAnim, Condition[] Conds, int affHitStunT, int affBlockStunT, int damage, int attKind, int attWeight,
      boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit){
        this( datnam, AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.Conds = Conds;
        this.attWeight = attWeight;
      }
  
  //Copy-Konstruktor // Arraykopieren
  Action(Action a){ //Probleme mit projektilen
    println(a.datnam);
    this.datnam = a.datnam;
    this.sprsIds = a.sprsIds;
    this.damage = a.damage; this.superNeed = a.superNeed;
    this.gravMult = a.gravMult; this.fricMult = a.fricMult;
    this.affHitStunT = a.affHitStunT; this.affBlockStunT = a.affBlockStunT; 
    this.attKind = a.attKind; this.attWeight = a.attWeight;  this.attRank = a.attRank;
    this.gravityActive = a.gravityActive; this.addingForce = a.addingForce; this.knocksDown = a.knocksDown; this.multiHit = a.multiHit;
    this.Conds = a.Conds; this.ActEffs = a.ActEffs; 
    if(a.AttAnim != null){
      this.AttAnim = new Animation(a.AttAnim);
    }
    /*this.HurtBoxCollect = a.HurtBoxCollect;
    this.HitBoxCollect = a.HitBoxCollect;
    this.whenToUpdBoxs = a.whenToUpdBoxs;
    this.setForceAtDur = a.setForceAtDur;*/
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
  
  void addEffs(ActEffect... Effs){
    ActEffect temp[] = new ActEffect[ActEffs.length+Effs.length];
    for(int i = 0; i < ActEffs.length; i++){
      temp[i] = ActEffs[i];
    }
    for(int i = 0; i < Effs.length; i++){
      temp[ActEffs.length+i] = Effs[i];
    }
    ActEffs = temp;
  }
  
  void playAction2(Fighter Pl, Fighter Opp, Action ToSetTo){
    boolean incrDur = true;
    
    for(ActEffect a : ActEffs){
      if(a.whereUsed == 1 && a.cond(Pl, Opp)){
        a.Effect(Pl, Opp);
      }
    }
    
    if(curCollumn == 0 && curMoveDur == 0){
      Pl.curSuper -= superNeed;
      if(AttAnim != null){
        Pl.CurAnimation = AttAnim;
        if(resetAnim){ AttAnim.Reset();}
        
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
  
  void clearAndCurBoxes(Fighter Pl){ // means adding current boxes
                Pl.HitBoxes.clear();
                Pl.HurtBoxes.clear();
       for(int i = 0; i < HitBoxCollect.get(curCollumn).size(); i++){
           Pl.HitBoxes.add(HitBoxCollect.get(curCollumn).get(i));
         }
         
       for(int i = 0; i < HurtBoxCollect.get(curCollumn).size(); i++){
           Pl.HurtBoxes.add(HurtBoxCollect.get(curCollumn).get(i));
         }
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
      for(ActEffect a : ActEffs) a.reset();
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

class PH_sp1 extends Action{  
      F_PHaus F = null;
  
  PH_sp1(String datnam, Animation AttAnim, Condition[] Conds, int affHitStunT, int affBlockStunT, int damage, int attKind, int attWeight,
      boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, F_PHaus F){
        super( datnam, AttAnim,Conds, affHitStunT, affBlockStunT, damage, attKind, attWeight, gravityActive, addingForce, knocksDown, multiHit);
        this.F = F;
      }
      
      void specialEffect(Fighter Pl, Fighter Opp){
        if(curCollumn == 1){
          F.curFartMeter+=30;
        }
      }

}

class DirChangeAct extends Action{
  DirChangeAct(String datnam, Animation AttAnim, Condition[] Conds, int affHitStunT, int affBlockStunT, int damage, int attKind, int attWeight,
      boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit){
        super( datnam, AttAnim, Conds, affHitStunT, affBlockStunT, damage, attKind, attWeight, gravityActive, addingForce, knocksDown, multiHit);
      }
      
  void specialEffectOnEnd(Fighter Pl, Fighter Opp){
           Pl.Force.x *= -1;
      Pl.dirMult *= -1;
      if(Pl.facing == Pl.RIGHT){
        Pl.facing = Pl.LEFT;
      }
      else if(Pl.facing == Pl.LEFT){
        Pl.facing = Pl.RIGHT;
      }
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

class ChgOEndAndHit extends Action{
  Action ActToChangeTo;
  
    ChgOEndAndHit(String datnam, Animation AttAnim, Condition[] Conds, int affHitStunT, int affBlockStunT, int damage, int attKind, int attWeight,
      boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super( datnam, AttAnim, Conds, affHitStunT, affBlockStunT, damage, attKind, attWeight, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo;
      }
          void specialEffectOnEnd(Fighter Pl, Fighter Opp){
        Pl.CurAction.reset();
        Pl.changeAction(ActToChangeTo);
        AttAnim.Reset();
  }  
    void specialEffectOnHit(Fighter Pl, Fighter Opp){
      reset();
      Pl.CurAction.reset();
      Pl.changeAction(ActToChangeTo);   
      curMoveDur = -1;
      AttAnim.Reset();
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
  
  ChangeOnEndAct(Action a, Action ActToChangeTo){
    super(a); this.ActToChangeTo = ActToChangeTo;
  }
  
    void setAct(Action a){
      ActToChangeTo = a;
    }
    Action getAct(){
      return ActToChangeTo;
    }
  
    void specialEffectOnEnd(Fighter Pl, Fighter Opp){
        Pl.CurAction.reset();
        Pl.changeAction(ActToChangeTo);
  }
  
}

class ChangeOnCondAct extends ChangeOnEndAct{
  
   ChangeOnCondAct(Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit, ActToChangeTo);
        this.ActToChangeTo = ActToChangeTo; hitCancel = false;
  }
  
   ChangeOnCondAct(String datnam, Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(datnam, AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit, ActToChangeTo); hitCancel = false;
  }
  
  ChangeOnCondAct(Action a, Action ActToChangeTo){
    super(a, ActToChangeTo);
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
    int holdTimerCur = 0, holdTimer, input = 0;
  
   HoldButToKeepAct(Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
   Action ActToChangeTo, int holdTimer, int input){
        super(AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo; this.holdTimer = holdTimer; this.input = input;
  }
  
   HoldButToKeepAct(String datnam, Animation AttAnim, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
   Action ActToChangeTo, int holdTimer, int input){
        super(datnam, AttAnim, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo; this.holdTimer = holdTimer; this.input = input;
  }
  
     void alwaysSpecialEffect(Fighter Pl, Fighter Opp){
             if(Pl.inputs[input] && holdTimerCur <= holdTimer){ //CAUTION: eventuelle Bugs durch cancels aufgrund von nicht resetens von holdTimerCur
          this.curCollumn = 0;
      }
      else{
        holdTimerCur = 0;
        Pl.changeAction(ActToChangeTo);
      }
      
      if(Pl.inputs[2]){
        Pl.Force.x = 6;
      }
      if(Pl.inputs[3]){
        Pl.Force.x = -6;
      }
      
      holdTimer++;
  }
  
     void specialEffectOnEnd(Fighter Pl, Fighter Opp){
        holdTimerCur = 0;
        Pl.changeAction(ActToChangeTo);
  }
  
}

class ChangeAction extends Action{
  Action ActToChangeTo;
  
        ChangeAction(int[] sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo; hitCancel = false;
  }
            ChangeAction(Animation sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo; hitCancel = false;
  }
          ChangeAction(String datnam, Animation sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, Action ActToChangeTo){
        super(datnam, sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.ActToChangeTo = ActToChangeTo; hitCancel = false;
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
        this.grabOppPos = grabOppPos; hitCancel = false;
  }
            GrabAction( Animation sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, float[][] grabOppPos){
        super( sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.grabOppPos = grabOppPos; hitCancel = false;
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
  
    ProjAction(String datnam, Animation AttAnim, Condition[] Conds, int affHitStunT, int affBlockStunT, int damage, int attKind, int attWeight, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
  float m, int exTimer, float fx, float fy, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
    super(datnam, AttAnim, Conds, affHitStunT, affBlockStunT, damage, attKind, attWeight, gravityActive, addingForce, knocksDown, multiHit);
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

class PHaus_Sp2 extends ProjAction{
  F_PHaus F;
  
    PHaus_Sp2(String datnam, Animation AttAnim, Condition[] Conds, int affHitStunT, int affBlockStunT, int damage, int attKind, int attWeight, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit,
  float m, int exTimer, float fx, float fy, boolean effByFric, boolean effByGrav, boolean destroyedByCol, F_PHaus F){
    super(datnam, AttAnim, Conds, affHitStunT, affBlockStunT, damage, attKind, attWeight, gravityActive, addingForce, knocksDown, multiHit, m, exTimer, fx, fy, effByFric, effByGrav, destroyedByCol);
    this.F = F;
  }
  
      void specialEffect(Fighter Pl, Fighter Opp){
    if(curCollumn == 1 && (Pl.x <= Camerabox.x - Camerabox.br/2 + 100 || Pl.x >= Camerabox.x + Camerabox.br/2 - 100 || (Pl.CollisionBox.rside || Pl.CollisionBox.lside)) ){
      for(int i = 1; i < 4; i++){
      PH_Apple p = new PH_Apple(Pl.x + 300*Pl.dirMult*i, -400, 80, 120,
      Pl.dirMult * fx , 0.2, m, exTimer, affHitStunT, affBlockStunT, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit, F);
      p.setAnims(ProjAnim, destrEffAnim); 
      Pl.Projectiles.add(p);
      }
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
    if(curCollumn == 1 && curMoveDur == 0){
      
      Projectile p = new SelfHitProj(Camerabox.x + Camerabox.br/2*Pl.dirMult, initHeight/2-100, 80, 80, Pl.dirMult * fx , fy, m, exTimer, affHitStunT, affBlockStunT, damage, effByFric, effByGrav, destroyedByCol);
      p.setAnims(ProjAnim, destrEffAnim);
      //p.Anim = new Animation(ProjAnim);
      //p.destrEff = new Animation(destrEffAnim); 
      
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

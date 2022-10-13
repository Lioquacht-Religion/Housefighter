class ActionModules{}

//later used for special Counterhiteffects and such
class ActEffect{
  // 0 = on Hit; 1 = always active or depended on conds; 2 = always active on touch;
  //if CounterEff, whereUsed Var is indicator for state of Opp , needs to be handled globally by base class;
  Condition[] Conds = new Condition[0]; int whereUsed = 0;
  ActEffect(){}
  ActEffect(int whereUsed, Condition[] Conds){
    this.whereUsed = whereUsed; this.Conds = Conds;
  }
  void draw(Fighter Pl, Fighter Opp){ Effect(Pl, Opp);} 
  void Effect(Fighter Pl, Fighter Opp){
  }
  
  boolean cond(Fighter Pl, Fighter Opp){
    if(Conds.length <= 0){ return true;}
    for(int i = 0; i < Conds.length; i++){
      if(!Conds[i].cond(Pl, Opp)){
        return false;
      }
    }
    return true;
  }
  
  void reset(){}
  
}



class GatlingEff extends ActEffect{
  Action[] ActList;
  
  GatlingEff(Action... ActList){
    this(new Condition[0], ActList);
  }
  GatlingEff(Condition[] Conds, Action... ActList){
    this.ActList = ActList; this.Conds = Conds; this.whereUsed = 1;
  }
  
  void Effect(Fighter Pl, Fighter Opp){
    if(Pl.cancelWindow <= 0) return;
    for(Action a : ActList){
      boolean moveValid = true;
      if(a == null){ continue;}
      for(Condition c : a.Conds){
        if(!c.cond(Pl, Opp)){ moveValid = false; break;}
      }
      if(moveValid){ Pl.changeAction(a); Pl.cancelWindow = 0; break;}
    }
  }

}

class SnglGatEff extends ActEffect{ //specifiy own Conds for Actchange
  Action ToChange;
  
  SnglGatEff(Action ToChange, Condition... Conds){
    this.ToChange = ToChange; this.Conds = Conds; this.whereUsed = 1;
  }
  
 void Effect(Fighter Pl, Fighter Opp){
    if(Pl.cancelWindow <= 0) return; Pl.changeAction(ToChange);
  }
  
}

class AddHP extends ActEffect{
  int addHP = 0;
  AddHP(int addHP, int whereUsed, Condition... Conds){
    this.addHP = addHP; this.whereUsed = whereUsed; this.Conds = Conds;
  }
  void Effect(Fighter Pl, Fighter Opp){Pl.curHP -= addHP;}
}

class OppEff extends ActEffect{ ActEffect Eff;
  OppEff(ActEffect Eff, int whereUsed, Condition... Conds){
    this.Eff = Eff; this.whereUsed = whereUsed; this.Conds = Conds;
  }
  void Effect(Fighter Pl, Fighter Opp){Eff.Effect(Opp, Pl);}
}

class setActCurColTime extends ActEffect{ int setCurCollumn = 0, setCurTime = 0;
  setActCurColTime(int setCurCollumn, int setCurTime, Condition... Conds){ this.setCurCollumn = setCurCollumn; this.setCurTime = setCurTime; this.Conds = Conds; this.whereUsed = 1; }
  void Effect(Fighter Pl, Fighter Opp){ if(setCurCollumn < Pl.CurAction.HitBoxCollect.size()){Pl.CurAction.curCollumn = setCurCollumn; Pl.CurAction.curMoveDur = setCurTime; };  }
}

class SetForce extends ActEffect{ float fx = 0, fy = 0;
  SetForce(float fx, float fy, int whereUsed, Condition... Conds){ this.fx = fx; this.fy = fy; this.whereUsed = whereUsed; this.Conds = Conds; }
  void Effect(Fighter Pl, Fighter Opp){ Pl.Force.x = fx*Pl.dirMult; Pl.Force.y = fy; }  
}

class SelfCancEff extends ActEffect{ Action Act = null;
  SelfCancEff(Action Act){ this.Act = Act; this.whereUsed = 1;}
  SelfCancEff(Action Act, int whereUsed, Condition[] Conds){ this.Act = Act; this.whereUsed = whereUsed; this.Conds = Conds;}
  void Effect(Fighter Pl, Fighter Opp){ 
    boolean actCond = true;
    for(Condition c : Act.Conds){ if(!c.cond(Pl, Opp)) actCond = false;}
  if(Act == Pl.CurAction && actCond && Pl.cancelWindow <= 0)Pl.changeAction(Act); 
}
}


class FirstTimeEff extends ActEffect{
  boolean FirstTime = true; ActEffect[] Effs;
  FirstTimeEff(Condition[] Conds, ActEffect... Effs){
    this.Effs = Effs; this.Conds = Conds; this.whereUsed = 1;
  }
  FirstTimeEff(ActEffect... Effs){
    this(new Condition[]{new Condition()}, Effs);
  }
  
  void Effect(Fighter Pl, Fighter Opp){ 
    
    if(FirstTime){
      for(ActEffect a : Effs){
        if(a.cond(Pl, Opp)) a.Effect(Pl, Opp);
      }
      FirstTime = false;
    }
 
  }
  void reset(){FirstTime = true;}
  
}

class TimerEff extends ActEffect{
  int curTime = 0; int maxTime = 2; ActEffect[] Effs;
  TimerEff(int maxTime, Condition[] Conds, ActEffect... Effs){
    this.maxTime = maxTime; this.Effs = Effs; this.Conds = Conds; this.whereUsed = 1;
  }
  
  TimerEff(int maxTime, ActEffect... Effs){
    this(maxTime, new Condition[]{new Condition()}, Effs);
  }
  
  void Effect(Fighter Pl, Fighter Opp){ 
    if(curTime < maxTime) curTime++;
    
    if(curTime >= maxTime){
      for(ActEffect a : Effs){
        if(a.cond(Pl, Opp)) a.Effect(Pl, Opp);
      }
      curTime = 0;
    }
 
  }
  
  void reset(){ curTime = 0;}
  
}

class PutInHitSt extends ActEffect{
  int hitStun = 0, addDamage = 0; 
  PutInHitSt(int hitStun, int addDamage, Condition... Conds){
    this.hitStun = hitStun; this.addDamage = addDamage; this.Conds = Conds; whereUsed = 1;
  }
  void Effect(Fighter Pl, Fighter Opp){
    VisEffectsList.add(new VisualEffect(Opp.x, Opp.y, HitEff, addDamage/4));
        Soundeffects[0].cue(0); Soundeffects[0].play();
    Opp.InHitStun.whenToUpdBoxs[0] = hitStun; Opp.curHP -= addDamage;
    Opp.changeAction(Opp.InHitStun);
  }

}

class GrabEff extends ActEffect{
  float[][] grabOppPos = new float[1][2]; int[] times; int grabCollumn = 0, grabTime = 0;
  GrabEff(float[][] l_grabOppPos, int[] times){
    this.whereUsed = 1; this.Conds = new Condition[]{new Condition()};
    this.grabOppPos = l_grabOppPos; this.times = times;
  }
  
  void Effect(Fighter Pl, Fighter Opp){
    if(Pl.CurAction.curCollumn == 0 && Pl.CurAction.curMoveDur == 0){ grabCollumn = 0; grabTime = 0;}
    if(times[grabCollumn] == grabTime && times.length-1 > grabCollumn){
      grabCollumn++;println(grabCollumn);
      grabTime = 0;
    }
    grabTime++;
    if(grabCollumn < times.length && times[grabCollumn] > grabTime ){
        Opp.x = Pl.x + grabOppPos[grabCollumn][0] * Pl.dirMult;
    Opp.y = Pl.y + grabOppPos[grabCollumn][1];
    Opp.curHP -= Pl.CurAction.damage;
    Opp.CurAction.reset();
    Opp.BeingGrapped.whenToUpdBoxs[0] = times[grabCollumn]+Pl.CurAction.affHitStunT;
    Opp.CurAction = Opp.BeingGrapped;
    //Opp.CurAction.reset(); Opp.BeingGrapped.reset();
    Opp.changeAction(Opp.BeingGrapped);
    Opp.throwInvu = 20;
    }
  }
}

class ChangeAnimTo extends ActEffect{ Animation ChangeTo;
  ChangeAnimTo(Animation ChangeTo, int whereUsed, Condition... Conds){
    this.ChangeTo = ChangeTo; this.whereUsed = whereUsed; this.Conds = Conds;
  }
    void Effect(Fighter Pl, Fighter Opp){
       Animation a = Pl.CurAction.AttAnim;
       if(!a.loop)a.Reset();
       if(!ChangeTo.loop)ChangeTo.Reset();
       Pl.CurAnimation = ChangeTo;
       Pl.CurAction.AttAnim = ChangeTo;
  }
  
}

class ChangeAnimCoords extends ActEffect{
  int ax = 0, ay = 0;
  ChangeAnimCoords(int ax, int ay, Condition... Conds){ this.ax = ax; this.ay = ay; this.whereUsed = 1; this.Conds = Conds; }
    void Effect(Fighter Pl, Fighter Opp){
      Animation a = Pl.CurAction.AttAnim; a.X_coords = ax; a.Y_coords = ay;
  }
}

class ProjAddEff extends ActEffect{
  Projectile P; int dirMult = 1; //Animation Anim, destrEff;
  ProjAddEff(Projectile P, int dirMult, Condition... Conds){ this.P = P; this.dirMult = dirMult; this.whereUsed = 1; this.Conds = Conds;}
  
  void Effect(Fighter Pl, Fighter Opp){ 
  Projectile Pr =  P.copy(); Pr.setXY(Pl.dirMult*this.dirMult, Pl.x, Pl.y); Pr.setAnims(P.Anim, P.destrEff); //Pr.dirMult = Pl.dirMult*this.dirMult; 
  //Pr.forcex = Pr.forcex*Pl.dirMult; 
  Pl.Projectiles.add(Pr);
  println(Pr.dirMult);
  }
}

class AddVisEff extends ActEffect{
  Animation VisEffAnim = null; int addX = 0, addY = 0;
  //VisualEffect(float x, float y, Animation Anim, float scale)
  AddVisEff(Animation VisEffAnim, int addX, int addY, int whereUsed, Condition... Conds){ 
    this.whereUsed = whereUsed; this.addX = addX; this.addY = addY; this.Conds = Conds; this.VisEffAnim = VisEffAnim; 
} 
  void Effect(Fighter Pl, Fighter Opp){ VisEffectsList.add( new VisualEffect( Pl.x+addX*Pl.dirMult, Pl.y+addY, VisEffAnim, 1, Pl.dirMult) ); }
}

class WH_stance extends ActEffect{  F_WHaus F = null;
  WH_stance( F_WHaus F, int whereUsed, Condition... Conds){super(whereUsed, Conds); this.F = F;}
    void Effect(Fighter Pl, Fighter Opp){
      F.stance = true;
    }
}

class FirstHitEff extends GravEff{
  FirstHitEff(int actCollumn, int actTime, boolean setGrav){
    super(actCollumn, actTime, setGrav);
  }
  void Effect(Fighter Pl, Fighter Opp){ Action PAct = Pl.CurAction;
    if(PAct.curCollumn == actCollumn && PAct.curMoveDur == actTime){ PAct.firstHit = setGrav;}
  }
}

class GravEff extends ActEffect{
  int actCollumn = 0, actTime = 0; boolean setGrav = true;
  GravEff(int actCollumn, int actTime, boolean setGrav){
    this.actCollumn = actCollumn; this.actTime = actTime; this.setGrav = setGrav;
    this.whereUsed = 1;
  }
  
  void Effect(Fighter Pl, Fighter Opp){ Action PAct = Pl.CurAction;
    if(PAct.curCollumn == actCollumn && PAct.curMoveDur == actTime){
      PAct.gravityActive = setGrav;
    }
  }
  
}

class ArmorEff extends ActEffect{
  int actCollumn, actTime, setArmor;
  ArmorEff(int actCollumn, int actTime, int setArmor){
    this.actCollumn = actCollumn; this.actTime = actTime; this.setArmor = setArmor;
    this.whereUsed = 1;
  }
  
  void Effect(Fighter Pl, Fighter Opp){ Action PAct = Pl.CurAction;
    if(PAct.curCollumn == actCollumn && PAct.curMoveDur == actTime){
      Pl.armorCount = this.setArmor;
    }
  }
  
}

class KDownEff extends GravEff{
  KDownEff(int actCollumn, int actTime, boolean setForceAdd){
    super(actCollumn, actTime, setForceAdd);
  }
  
  void Effect(Fighter Pl, Fighter Opp){ Action PAct = Pl.CurAction;
    if(PAct.curCollumn == actCollumn && PAct.curMoveDur == actTime){
      PAct.knocksDown = setGrav;
    }
  }
  
}

class ForceAddEff extends GravEff{
  ForceAddEff(int actCollumn, int actTime, boolean setForceAdd){
    super(actCollumn, actTime, setForceAdd);
  }
  
  void Effect(Fighter Pl, Fighter Opp){ Action PAct = Pl.CurAction;
    if(PAct.curCollumn == actCollumn && PAct.curMoveDur == actTime){
      PAct.addingForce = setGrav;
    }
  }
  
}

class ResAnimEff extends ActEffect{
  
  ResAnimEff(int whereUsed, Condition... Conds){
    this.whereUsed = whereUsed; this.Conds = Conds;
  }
  void Effect(Fighter Pl, Fighter Opp){
    Pl.CurAnimation.Reset();
  }
  
}

class SoundEff extends ActEffect{
  int actCollumn = 0, actTime = 0; AudioPlayer Sound = null; 
  SoundEff(int soundId, int actCollumn, int actTime){
    this.whereUsed = 1; this.Sound = Soundeffects[soundId]; this.actCollumn = actCollumn; this.actTime = actTime;
  }
  SoundEff(String SoundName, int actCollumn, int actTime){
    this.whereUsed = 1; this.Sound = minim.loadFile("Soundeffekte/"+SoundName+".wav"); this.actCollumn = actCollumn; this.actTime = actTime;
  }
 void Effect(Fighter Pl, Fighter Opp){
   if(Pl.CurAction.curCollumn == actCollumn && Pl.CurAction.curMoveDur == actTime){
     Sound.cue(0); Sound.play();
   }
  }
  
}

class CounterEff extends ActEffect{
  CounterEff(int whereUsed){this.whereUsed = whereUsed;}
    CounterEff(int whereUsed, Condition... Conds){ super(whereUsed, Conds);}

  void Effect(Fighter Pl, Fighter Opp){
  }
}

class WallBounceEff extends ActEffect{
    WallBounceEff(int whereUsed, Condition[] Conds){
       super(whereUsed, Conds);
  }
  void Effect(Fighter Pl, Fighter Opp){
    if(new Grounded().cond(Pl, Opp)){
      Pl.Force.y *= -1; Pl.gforce = 0; slowMoDur = 9; slowMoValue = 3;
    }
    else if(Pl.x - Pl.CollisionBox.br/2 - 10 <= Camerabox.x - Camerabox.br/2 || Pl.x + Pl.CollisionBox.br/2 + 10 >= Camerabox.x + Camerabox.br/2 ){
      Pl.WallBounce.AttAnim = Pl.wBflightF; Pl.WallBounce.AttAnim.Reset(); Pl.CurAnimation = Pl.wBflightF;
      Pl.Force.x *= -1; Pl.gforce = 0; slowMoDur = 20; slowMoValue = 4;
    }
  } 
}

class AddOwnForcToOpp extends ActEffect{
  float mult = 1.;
  
  AddOwnForcToOpp(int whereUsed, float mult, Condition... Conds){
    super(whereUsed, Conds); this.mult = mult;
  }
  
   void Effect(Fighter Pl, Fighter Opp){
     Opp.Force.x = Pl.Force.x * mult;
     Opp.Force.y = Pl.Force.y * mult;
   }
  
}

class ChangeActTo extends ActEffect{Action ActToSetTo;
  ChangeActTo(Action ActToSetTo, int whereUsed, Condition... Conds){
    super(whereUsed, Conds); this.ActToSetTo = ActToSetTo;
  }
     void Effect(Fighter Pl, Fighter Opp){
       Pl.changeAction(ActToSetTo);
     }
  
}

class OBS_setSFToM extends ActEffect{ //set Stands Force to Master's Force
  F_OBHaus F;
  OBS_setSFToM(F_OBHaus F, int whereUsed, Condition... Conds){ this.F = F; this.whereUsed = whereUsed; this.Conds = Conds;}
  void Effect(Fighter Pl, Fighter Opp){F.Stand.Force.x = F.Force.x; F.Stand.Force.y = F.Force.y;}
}

class OBS_changeXY extends ActEffect{ F_OBHaus F; int x = 0, y = 0;
  OBS_changeXY(F_OBHaus F, int x, int y, Condition... Conds){this.F = F; this.x = x; this.y = y; this.whereUsed = 1; this.Conds = Conds;}
  void Effect(Fighter Pl, Fighter Opp){F.Stand.x = F.x + x*F.Stand.dirMult; F.Stand.y = F.y + y;} 
}

class changeToOwnXY extends ActEffect{int x = 0, y = 0;
  changeToOwnXY(int x, int y, Condition... Conds){this.x = x; this.y = y; this.whereUsed = 1; this.Conds = Conds;}
  void Effect(Fighter Pl, Fighter Opp){Pl.x += this.x*Pl.dirMult; Pl.y += this.y;} 
}

class OBS_setState extends ActEffect{
  F_OBHaus F; int setTo = 0;
  OBS_setState(F_OBHaus F, int setTo, int whereUsed){ this.F = F; this.setTo = setTo; this.whereUsed = whereUsed;}
  OBS_setState(F_OBHaus F, int setTo, int whereUsed, Condition... Conds){ this.F = F; this.setTo = setTo; this.whereUsed = whereUsed; this.Conds = Conds;}
  void Effect(Fighter Pl, Fighter Opp){F.Stand.State = setTo;} 
}

class OBS_setAction extends ActEffect{
  F_OBHaus F; Action setTo = null;
  OBS_setAction(F_OBHaus F, Action setTo, int whereUsed){ this.F = F; this.setTo = setTo; this.whereUsed = whereUsed;}
  OBS_setAction(F_OBHaus F, Action setTo, int whereUsed, Condition... Conds){ this.F = F; this.setTo = setTo; this.whereUsed = whereUsed; this.Conds = Conds;}
  void Effect(Fighter Pl, Fighter Opp){F.Stand.changeAction(setTo);} 
}

class OBS_setStateAndAct extends ActEffect{
  F_OBHaus F; Action setTo = null; int state = 0;
  OBS_setStateAndAct(F_OBHaus F, Action setTo, int state, int whereUsed){ this.F = F; this.setTo = setTo; this.state = state; this.whereUsed = whereUsed;}
  OBS_setStateAndAct(F_OBHaus F, Action setTo, int state, int whereUsed, Condition... Conds){ this.F = F; this.setTo = setTo; this.state = state; this.whereUsed = whereUsed; this.Conds = Conds;}
  void Effect(Fighter Pl, Fighter Opp){F.Stand.changeAction(setTo); F.Stand.State = state;} 
}

class PH_dirEff extends ActEffect{
  float fx = 0, fy = 0, rot = 0; int dir = 1;
  PH_dirEff(int whereUsed, Condition[] Conds, float fx, float fy, float rot, int dir ){
    super(whereUsed, Conds); this.fx = fx; this.fy = fy; this.rot = rot; this.dir = dir;
  }
  
    void Effect(Fighter Pl, Fighter Opp){
      Action PAct = Pl.CurAction;
      PAct.AttAnim.rot = rot;
      //if(PAct.curCollumn == 2 && PAct.curMoveDur == 1){
        PAct.updFrameDataArr_float(2, fx, fy);
        //Pl.dirMult *= dir;       
      //}
  }
  
}

class WH_gearAdd extends ActEffect{
  private F_WHaus F; private int gearAdd = 0;
  public WH_gearAdd(int gearL, F_WHaus F, int whereUsed, Condition... Conds){ 
  this.gearAdd = gearL; this.F = F; this.whereUsed = whereUsed; this.Conds = Conds;}
    void Effect(Fighter Pl, Fighter Opp){
      if(F.gearLevel+gearAdd >= 0 && F.gearLevel+gearAdd <= 3)
      F.gearLevel += gearAdd;
    }    
}

class Condition extends ActionModules{
  Condition(){}
  
  boolean cond(Fighter Pl, Fighter Opp){
    return true;
  }
  void EffectIfCond(Fighter Pl, Fighter Opp){
  }
}

class CamWallTouch extends Condition{
  
  boolean cond(Fighter Pl, Fighter Opp){
    if((Pl.x - Pl.CollisionBox.br/2 - 10 <= Camerabox.x - Camerabox.br/2) || (Pl.x + Pl.CollisionBox.br/2 + 10 >= Camerabox.x + Camerabox.br/2) ){
      return true;
    }
    return false;
  }
  
}
    
class xDistOpp extends Condition{
  int dist = 0; boolean lt = true; xDistOpp(int dist, boolean lt){this.dist = dist; this.lt = lt;} 
  boolean cond(Fighter Pl, Fighter Opp){ return (lt && abs(Pl.x - Opp.x) <= dist) || (!lt && abs(Pl.x - Opp.x) >= dist);}
}

class FalseCond extends Condition{
  Condition[] fConds;
  public FalseCond(Condition... fConds){this.fConds = fConds;}
  boolean cond(Fighter Pl, Fighter Opp){ 
    for(Condition c : fConds){
      if(c.cond(Pl, Opp)) return false;
  }
return true;
}
  
}


class OBSH_checkState extends Condition{
  private F_OBHaus F; int state = 0; boolean b = true; 
  OBSH_checkState(F_OBHaus F, int state, boolean b){this.F = F; this.state = state; this.b = b;} 
   boolean cond(Fighter Pl, Fighter Opp){return ((F.Stand.State == state) && b) || ((F.Stand.State != state) && !b);}
}

class OBSH_checkAction extends Condition{
  private F_OBHaus F; Action Act[]; boolean b = true; 
  OBSH_checkAction(F_OBHaus F, Action Act, boolean b){this.F = F; this.Act = new Action[]{Act}; this.b = b;} 
  OBSH_checkAction(F_OBHaus F, boolean b, Action... Act){this.F = F; this.Act = Act; this.b = b;}
   boolean cond(Fighter Pl, Fighter Opp){
     for(Action a : this.Act){
       if( ((F.Stand.CurAction == a) && b) || ((F.Stand.CurAction != a) && !b) ) return true;
     }
     return false;
 }
}

class WH_checkStance extends Condition{
  private F_WHaus F; boolean b = true; WH_checkStance(F_WHaus F, boolean b){this.F = F; this.b = b;} boolean cond(Fighter Pl, Fighter Opp){return F.stance == b;}
}

class WH_gearCheck extends Condition{
  private F_WHaus F; private int gearL_comp = 0;
  public WH_gearCheck(int gearL, F_WHaus F){ this.gearL_comp = gearL; this.F = F;}
    boolean cond(Fighter Pl, Fighter Opp){ return gearL_comp <= F.gearLevel;}
}

class AnimCheck extends Condition{ //doesnt do anything
  private Animation compAnim;AnimCheck(Animation compAnim){this.compAnim = compAnim;}
  boolean cond(Fighter Pl, Fighter Opp){ 
  return compAnim == Pl.CurAction.AttAnim || compAnim == Pl.CurAnimation;// || (compAnim != Pl.CurAction.AttAnim && !b); 
}
}

class OppCheck extends Condition{
  Condition[] Conds;
  OppCheck(Condition... Conds){ this.Conds = Conds; }
  
  boolean cond(Fighter Pl, Fighter Opp){
    for(Condition c : Conds){
      if(c.cond(Opp, Pl)); else return false;
    }
    return true;
  }
}

class OrCond extends Condition{ //or not xor
  Condition[] Conds1, Conds2;  ArrayList<Condition[]> AllOrs = new ArrayList<Condition[]>();
  OrCond(Condition[] Conds1, Condition[] Conds2){
    this.Conds1 = Conds1; this.Conds2 = Conds2;
  }
  
  OrCond(Condition Conds1, Condition Conds2){
    this.Conds1 = new Condition[]{Conds1}; this.Conds2 = new Condition[]{Conds2};
  }
  
  OrCond(Condition[]... Conds){
    for(int i = 0; i < Conds.length; i++){
      AllOrs.add(Conds[i]);
    }
  }
  OrCond(Condition... Conds){
    for(int i = 0; i < Conds.length; i++){
      AllOrs.add(new Condition[]{Conds[i]});
    }
  }
  
  boolean cond(Fighter Pl, Fighter Opp){
    boolean boo1 = true, boo2 = true;
    for(Condition c : Conds1){
      if(!c.cond(Pl, Opp)){
        boo1 = false;
        break;
      }
    }
    for(Condition c : Conds2){
      if(!c.cond(Pl, Opp)){
        boo2 = false;
        break;
      }
    }
    
    if(boo1 || boo2){
      return true;
    }
    else{
      return false;
    }
    
  }
  
}

class CheckStateCond extends Condition{
  Action State = null;
  CheckStateCond(Action State){ this.State = State; }
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.CurAction == State;
  }
  
}

class CheckAttPart extends Condition{
  int part = 0; CheckAttPart(int part){this.part = part;}
  boolean cond(Fighter Pl, Fighter Opp){ Action pact = Pl.CurAction; ArrayList<ColCircle> hl = pact.HitBoxCollect.get(pact.curCollumn);
    switch(part){
      case 1: return hl.size() > 0;
      
      case 0: int st = 0;
          for(int i = 0; i < pact.HitBoxCollect.size(); i++){
      if(pact.HitBoxCollect.get(i).size() == 0){ st++;}else {break;}
    }
      return pact.curCollumn <= st;
      
    case 2: int el = 0;
              for(int i = 0; i < pact.HitBoxCollect.size(); i++){
      if(pact.HitBoxCollect.get(i).size() > 0){ el = i;}
    }
    return pact.curCollumn >= el;
    
    default:
    return false;
  }
}
}

class ActTimeCond extends Condition{
  int actCurCollumn = 0, actCurTime = 0, actCurCollumn2 = 0, actCurTime2 = 0; //checks for between time 1 and time 2
  ActTimeCond(int actCurCollumn, int actCurTime){
    this.actCurCollumn = actCurCollumn; this.actCurTime = actCurTime;
  }
  ActTimeCond(int actCurCollumn, int actCurTime, int actCurCollumn2, int actCurTime2){
    this.actCurCollumn = actCurCollumn; this.actCurTime = actCurTime; this.actCurCollumn2 = actCurCollumn2; this.actCurTime2 = actCurTime2;
  }
  boolean cond(Fighter Pl, Fighter Opp){Action PAct = Pl.CurAction;
    return (PAct.curCollumn >= actCurCollumn && PAct.curCollumn <= actCurCollumn2) && (PAct.curMoveDur >= actCurTime && PAct.curMoveDur <= actCurTime2);
    //return (PAct.curCollumn > actCurCollumn && PAct.curCollumn < actCurCollumn2) 
    //|| ( PAct.curCollumn == actCurCollumn && PAct.curMoveDur >= actCurTime )|| ( PAct.curCollumn == actCurCollumn2 && PAct.curMoveDur <= actCurTime2 );
  }
  
}

class ActTimeCond2 extends Condition{
  int actCurCollumn = 0, actCurTime = 0;
  ActTimeCond2(int actCurCollumn, int actCurTime){
    this.actCurCollumn = actCurCollumn; this.actCurTime = actCurTime;
  }

  boolean cond(Fighter Pl, Fighter Opp){Action PAct = Pl.CurAction;
    return PAct.curCollumn == actCurCollumn && PAct.curMoveDur == actCurTime;
  }
  
}

class ActTimeCond3 extends Condition{
  int actCurCollumn = 0, actCurCollumn2 = 0; //checks for between time 1 and time 2
  ActTimeCond3(int actCurCollumn, int actCurCollumn2){
    this.actCurCollumn = actCurCollumn; this.actCurCollumn2 = actCurCollumn2;
  }

  boolean cond(Fighter Pl, Fighter Opp){Action PAct = Pl.CurAction;
    return (PAct.curCollumn >= actCurCollumn && PAct.curCollumn <= actCurCollumn2);
  }
  
}

class UseEffCond extends Condition{ //reassigns Eff Array to passed Action; consider renaming
  Condition[] Conds; ActEffect[] Effs; Action toAssignTo;
  UseEffCond(Action toAssignTo, Condition[] Conds, ActEffect[] Effs){
    this.toAssignTo = toAssignTo; this.Conds = Conds; this.Effs = Effs;
  }
  boolean cond(Fighter Pl, Fighter Opp){
    boolean boo = true;
    for(Condition c : Conds){
      if(!c.cond(Pl, Opp)){
        boo = false;
      }
    }
    
    if(boo){
      toAssignTo.ActEffs = Effs;
    }
    
    return boo;
  }
  
}



class CounterHit extends Condition{//to inspect Opponent not Player
  boolean cond(Fighter Pl, Fighter Opp){
    return Opp.counterState;
  }
}

class SuperCond extends Condition{
  int superNeed = 0;
  SuperCond(int superNeed){this.superNeed = superNeed;}
  
  boolean cond(Fighter Pl, Fighter Opp){
    return this.superNeed <= Pl.curSuper;
  }
}

class Grounded extends Condition{
    boolean cond(Fighter Pl, Fighter Opp){
    return Pl.CollisionBox.bottom || Pl.y >= GROUNDHEIGHT;
  }
}

class InAir extends Condition{
    boolean cond(Fighter Pl, Fighter Opp){
    return Pl.y < GROUNDHEIGHT && !Pl.CollisionBox.bottom;
  }
}

class dirCombCond extends Condition{
  int[] motion = {5};
  dirCombCond(int[] motion){this.motion = motion;}
  int[] getMotion(){ return motion; }
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.compareBufferWithCombAtt(motion);
  }
}

class ButCond extends Condition{
  int ButIndex = 0;
  ButCond(int ButIndex){ this.ButIndex = ButIndex;}
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.inputs[ButIndex];
  }
}

class ChargeCheck extends ButCond{
  int chargeAmount = 0;
  ChargeCheck(int ButIndex, int chargeAmount){ super(ButIndex); this.chargeAmount = chargeAmount;}
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.inputChargeT[ButIndex] >= chargeAmount;
  }
}

class ChargeDirCheck extends Condition{
  int chargeAmount = 0, dir = 1;
  ChargeDirCheck(int chargeAmount, int dir){ this.chargeAmount = chargeAmount; this.dir = dir;}
  boolean cond(Fighter Pl, Fighter Opp){
    return (Pl.inputChargeT[2] >= chargeAmount && Pl.dirMult*dir == -1) || (Pl.inputChargeT[3] >= chargeAmount && Pl.dirMult*dir == 1);//Pl.ChargeDirCheck(chargeAmount, dir);
  }
}

class VertForceCheck extends Condition{
  float speedNeed = 0; VertForceCheck(float speedNeed){this.speedNeed = speedNeed;}
  boolean cond(Fighter Pl, Fighter Opp){
    return (speedNeed >= 0 && ( (Pl.Force.x >= speedNeed && Pl.dirMult == 1) || (Pl.Force.x*-1 >= speedNeed && Pl.dirMult == -1) ) )
    || (speedNeed <= 0 && ( (Pl.Force.x >= speedNeed*-1 && Pl.dirMult == -1) || (Pl.Force.x*-1 >= speedNeed*-1 && Pl.dirMult == 1) ) );
  }  
}

class fPButCond extends ButCond{
  fPButCond(int ButIndex){ super(ButIndex);}
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.inputs[ButIndex] && Pl.firstPressInp[ButIndex];
  }
}

class dirHorFPButC extends ButCond{ // for dir CommandNormals
  int holdBut = 0;
  dirHorFPButC(int ButIndex, int holdBut){ super(ButIndex); this.holdBut = holdBut;}
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.inputs[ButIndex] && Pl.firstPressInp[ButIndex] && Pl.inputs[holdBut];
  }
}

class facingCond extends Condition{
  int mult = 1;// forward facing
  facingCond(int mult){
    this.mult = mult;
  }
  
  boolean cond(Fighter Pl, Fighter Opp){
    return ( (Pl.inputs[2] && Pl.dirMult*mult == 1) || (Pl.inputs[3] && Pl.dirMult*mult == -1) );
  }
}

class comfPButC extends Condition{
  int[][] motions = {{5},{5}};
  comfPButC(int[][] motions){ this.motions = motions;}
  int[] getMotion(){ return motions[0]; }
  boolean cond(Fighter Pl, Fighter Opp){
    for(int i = 0; i < motions.length; i++){
     if(Pl.compareBufferWithCombAtt(motions[i])){
       return true;
     }
    }
    return false;
  }
}

class FirstHitCheck extends Condition{
  FirstHitCheck(){}
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.CurAction.firstHit;
  }
}

class PH_FartCond extends Condition{
  int fartNeed = 0, fartConsume = 0; F_PHaus F;
  PH_FartCond(int fartNeed, int fartConsume, F_PHaus F){ this.fartNeed = fartNeed; this.fartConsume = fartConsume; this.F = F;}
  
  boolean cond(Fighter Pl, Fighter Opp){
    if(fartNeed <= F.curFartMeter){
      F.curFartMeter -= fartConsume;
      return true;
    }
    return false;
  }
  
}

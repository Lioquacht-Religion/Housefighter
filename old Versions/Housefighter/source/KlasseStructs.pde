class Structs{
  
}

class Animation extends Structs{
  int curCollumn = 0;
  int timer = 0;
  
  int[] changeTimes;
  
  int X_coords;
  int Y_coords;
  
  int[] sprite_ID;
  
  PImage[] Sprites;
  
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
    this.changeTimes = changeTimes;
    this.X_coords = X_coords;
    Sprites = new PImage[Spr_Size_Arr];
    for(int i = 0; i < Spr_Size_Arr; i++){
      Sprites[i] = loadImage(datnam + i + ".png");
    }
  }
  
  Animation(int[] changeTimes, int X_coords, int Y_coords, int Spr_Size_Arr, String datnam){
    this.changeTimes = changeTimes;
    this.X_coords = X_coords;
    this.Y_coords = Y_coords;
    Sprites = new PImage[Spr_Size_Arr];
    for(int i = 0; i < Spr_Size_Arr; i++){
      Sprites[i] = loadImage(datnam + i + ".png");
    }
  }
  
    Animation(int[] changeTimes, int X_coords, int Y_coords, PImage[] Sprites){
    this.changeTimes = changeTimes;
    this.X_coords = X_coords;
    this.Y_coords = Y_coords;
    this.Sprites = Sprites;
  }
  
  Animation(Animation toCopy){
    this.changeTimes = toCopy.changeTimes;
    this.X_coords = toCopy.X_coords;
    this.Y_coords = toCopy.Y_coords;
    this.Sprites = toCopy.Sprites;
  }
  
  void handleAnim(){
    boolean incrTimer = true;
    if(curCollumn == 0 && timer <= 0){
      
    }
    else if( curCollumn < Sprites.length-1 && timer >= changeTimes[curCollumn]){
      curCollumn++;
      timer = 0;
    }
    else if( curCollumn >= Sprites.length-1 && loop){
      curCollumn = 0;
      timer = 0;
      incrTimer = false;
    }
    
    if(incrTimer){
      timer++;
    }
  }
  
  void Reset(){
    timer = 0;
    curCollumn = 0;
  }
  
  
}

class Projectile extends Structs{
  final int MID = 0;
  final int HIGH = 1;
  final int LOW = 2;
  
  float x;
  float y;
  int br = 0;
  int ho = 0;
  
  float forcex;
  float forcey;
  
  float m = 1.2;
  
  int attKind = 3;
  int exTimer = 200;
  int hitStun = 10;
  int blockStun = 5;
  int damage = 10;
  
  boolean effByFric = false;
  boolean effByGrav = false;
  boolean destroyedByCol = false;
  boolean destroyedByHit = true;
  
  PImage Spr;
  Animation Anim = null;
  Animation destrEff = null;
  
  ColCircle HitBox;
  
  Projectile(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
    this.x = x;
    this.y = y;
    this.br = br;
    this.ho = ho;
    this.forcex = forcex;
    this.forcey = forcey;
    this.m = m;
    this.exTimer = exTimer;
    this.hitStun = hitStun;
    this.blockStun = blockStun;
    this.damage = damage;
    this.effByFric = effByFric;
    this.effByGrav = effByGrav;
    this.destroyedByCol = destroyedByCol;
    this.HitBox = new ColCircle(0, 0, br, ho, forcex, forcey, exTimer);
        HitBox.x = x;
    HitBox.y = y;
    Spr = Proj_sprs[0];
    
  }
  
    Projectile(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit){
      this(x, y, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol);
      this.destroyedByHit = destroyedByHit;
    }
  
 void specialStuff(Fighter Pl, Fighter Opp){
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
    x += forcex;
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
        //fill(255, 0, 0);
        //HitBox.draw();
    
    pushMatrix();
    translate( x, y);
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

class VisualEffect extends Structs{
  float x, y = 0;
  
  int exTimer = 30; 
  float scale = 0;
  Animation Anim;
  
  
  VisualEffect(float x, float y, Animation Anim, float scale){
    this.Anim = new Animation(Anim);
    this.Anim.Reset();
    this.x = x;
    this.y = y;
    this.scale = scale;
    this.exTimer = sumOfArr(Anim.changeTimes);
  }
  
  void draw(){
    Anim.handleAnim();
    pushMatrix();
    translate(x,y);
    imageMode(CENTER);
    if(scale > 1){
    scale((scale+1)*0.6, scale+1);
    }
    image(Anim.Sprites[Anim.curCollumn], 0, 0);
    popMatrix();
    
    if(exTimer > 0){
      exTimer--;
    }
    
  }
  
}

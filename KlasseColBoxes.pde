class ColBox{
  
  void draw(){
  }
}


class ColCircle extends ColBox{
  float x = 0;
  float y = 0;
  float addx, addy;
  int br = 20;
  int ho = 20;
  
  int exTimer = -1;
  
  float forcex = 0;
  float forcey = 0;
  
  ColCircle(){
  }
  
  ColCircle(float addx, float addy, int br, int ho){
    this.addx = addx;
    this.addy = addy;
    this.br = br;
    this.ho = ho;
  }
  
  ColCircle(float addx, float addy, int br, int ho, float forcex, float forcey, int exTimer){
    this.addx = addx;
    this.addy = addy;
    this.br = br;
    this.ho = ho;
    this.forcex = forcex;
    this.forcey = forcey;
    this. exTimer = exTimer;
  }
  
  //Copy-Konstruktor
  ColCircle(ColCircle c){
    this.addx = c.addx;
    this.addy = c.addy;
    this.br = c.br;
    this.ho = c.ho;
    this.forcex = c.forcex;
    this.forcey = c.forcey;
    this. exTimer = c.exTimer;
  }
  
  void draw(int dir){
    rect(x - br/2 + addx*dir, y - ho/2 + addy, br, ho);
    line(x + addx*dir, y +addy, x + (addx + forcex*5)*dir, y +addy + forcey*5);
  }
  
  void setxy(float x2, float y2){
     this.x = x2;
     this.y = y2;
  }
  
  boolean compare(ColCircle c, int dir, int dir2){
    
    return recRecColCheck(x - br/2 + addx*dir, y- ho/2 + addy, br, ho, c.x - c.br/2 + c.addx*dir2, c.y - c.ho/2 + c.addy, c.br, c.ho);
  }
  
}

class ColRect extends ColBox{
    float x = 0;
  float y = 0;
  float addx, addy;
  int br = 20;
  int ho = 20;
  
  boolean top = false, bottom = false, rside = false, lside = false;
  
  int exTimer = -1;
  
  float forcex = 0;
  float forcey = 0;
  
  ColRect(){
  }
  
    ColRect(float addx, float addy, int br, int ho){
    this.addx = addx;
    this.addy = addy;
    this.br = br;
    this.ho = ho;
  }
  
  ColRect(float x, float y, float addx, float addy, int br, int ho){
    this.x = x;
    this.y = y;
    this.addx = addx;
    this.addy = addy;
    this.br = br;
    this.ho = ho;
  }
  
  ColRect(float addx, float addy, int br, int ho, float forcex, float forcey, int exTimer){
    this.addx = addx;
    this.addy = addy;
    this.br = br;
    this.ho = ho;
    this.forcex = forcex;
    this.forcey = forcey;
    this. exTimer = exTimer;
  }
  
  //Copy-Konstruktor
  ColRect(ColRect c){
    this.addx = c.addx;
    this.addy = c.addy;
    this.br = c.br;
    this.ho = c.ho;
    this.forcex = c.forcex;
    this.forcey = c.forcey;
    this. exTimer = c.exTimer;
  }
  
  void draw(){
    rect(x + addx, y + addy - ho, br, ho);
  }
  
  void setColBools(boolean toSet){
    bottom = toSet; top = toSet; lside = toSet; rside = toSet;
  }
  
  void colCheckRect2( Fighter Pl, ColRect other){ //für Fighter - ColRect Kollisionen
      ColRect Pc = Pl.CollisionBox;
    if(Pl.y <= other.y - other.ho && Pl.y + Pl.Force.y >= other.y - other.ho 
    //&& Pl.x >= other.x && Pl.x <= other.x + other.br
    && (( Pc.x + Pc.br >= other.x && Pc.x <= other.x ) || ( Pc.x + Pc.br >= other.x + other.br && Pc.x <= other.x + other.br) || (Pc.x >= other.x && Pc.x + Pc.br <= other.x + other.br))
    ){
        Pc.bottom = true; other.top = true;
    }
   else
   if(Pl.y - Pc.ho + Pl.Force.y <= other.y && Pl.y - Pc.ho >= other.y
     && (( Pc.x + Pc.br >= other.x && Pc.x <= other.x ) || ( Pc.x + Pc.br >= other.x + other.br && Pc.x <= other.x + other.br) || (Pc.x >= other.x && Pc.x + Pc.br <= other.x + other.br))
     ){

      Pc.top = true; other.bottom = true;
    }
      //komplexer als punkt rect collision
     if(Pl.x + Pc.br/2 <= other.x && Pl.x + Pc.br/2 + abs(Pl.Force.x) >= other.x 
     //&& Pl.y >= other.y - other.ho && Pl.y <= other.y 
     && ( ( Pc.y > other.y && Pc.y - Pc.ho < other.y ) || ( Pc.y > other.y - other.ho && Pc.y - Pc.ho < other.y - other.ho) || (Pc.y < other.y && Pc.y - Pc.ho > other.y - other.ho))
     ){
      Pc.rside = true; other.lside = true;
    }
    
     if(Pl.x - Pc.br/2 >= other.x + other.br && Pl.x - Pc.br/2 - abs(Pl.Force.x) <= other.x + other.br
     //&& Pl.y >= other.y - other.ho && Pl.y <= other.y 
     && ( ( Pc.y > other.y && Pc.y - Pc.ho < other.y ) || ( Pc.y > other.y - other.ho && Pc.y - Pc.ho < other.y - other.ho) || (Pc.y < other.y && Pc.y - Pc.ho > other.y - other.ho))
     ){
      Pc.lside = true; other.rside = true;
    }
     
  }
  
    void colCheckRect( Fighter Pl, Fighter Opp){ // für Fighter - Fighter Kollisionen
      ColRect Pc = Pl.CollisionBox;
      ColRect other = Opp.CollisionBox;
    
    if(Pc.y + Pl.Force.y >= other.y - other.ho && Pc.y + Pl.Force.y <= other.y 
     && (( Pc.x + Pc.br >= other.x && Pc.x <= other.x ) || ( Pc.x + Pc.br >= other.x + other.br && Pc.x <= other.x + other.br) || (Pc.x >= other.x && Pc.x + Pc.br <= other.x + other.br))
    ){
        //bottom = true;
    }
    else{
      bottom = false;
    }
    
     if(Pc.y - Pc.ho + Pl.Force.y >= other.y && Pc.y - Pc.ho + Pl.Force.y <= other.y -other.ho
     && (( Pc.x + Pc.br >= other.x && Pc.x <= other.x ) || ( Pc.x + Pc.br >= other.x + other.br && Pc.x <= other.x + other.br) || (Pc.x >= other.x && Pc.x + Pc.br <= other.x + other.br))
     ){

      //top = true;
    }else{
      top = false;
    }
    
     if(Pc.x + Pc.br + Pl.Force.x >= other.x && Pc.x + Pc.br + Pl.Force.x <= other.x + other.br 
     && (( Pc.y >= other.y && Pc.y - Pc.ho <= other.y ) || ( Pc.y >= other.y - other.ho && Pc.y - Pc.ho <= other.y - other.ho) || (Pc.y <= other.y && Pc.y - Pc.ho >= other.y - other.ho))
     ){

      rside = true;
    }else{
      rside = false;
    }
    
     if(Pc.x + Pl.Force.x >= other.x && Pc.x + Pl.Force.x <= other.x + other.br 
     && (( Pc.y >= other.y && Pc.y - Pc.ho <= other.y ) || ( Pc.y >= other.y - other.ho && Pc.y - Pc.ho <= other.y - other.ho) || (Pc.y <= other.y && Pc.y - Pc.ho >= other.y - other.ho))
     ){

      lside = true;
    }else{
      lside = false;
    }
     
  }

  boolean compare(ColRect c){
    
    return recRecColCheck(x - br/2 + addx, y- ho/2 + addy, br, ho, c.x - c.br/2 + c.addx, c.y - c.ho/2 + c.addy, c.br, c.ho);
  }
  
 boolean ColRectLineVerCheck(int x){
   if(this.x - this.br/2 <= x && x <= this.x + this.br/2){
     return true;
   }
   return false;
 }
  
}

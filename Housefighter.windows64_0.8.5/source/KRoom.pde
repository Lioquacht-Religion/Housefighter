class Room{
  int roomWUnits = 1000, roomHUnits = 1000; 
  final static int cellsize = 60; 
  ArrayList<PImage> SprM0Sprites = new ArrayList<PImage>();
  ArrayList<String> SprDatPaths = new ArrayList<String>();
  int[][] SprIDMap0 = new int[100][100];
  boolean[][] ColMap0 = new boolean[100][100];
  ArrayList<ColRect> ColEbene = new ArrayList<ColRect>();
  ArrayList<Fighter> Players = new ArrayList<Fighter>(); 
  ArrayList<Fighter> Enemies = new ArrayList<Fighter>();
  
  Room(int roomWUnits, int roomHUnits, String... DatPaths){
    this.roomWUnits = roomWUnits; this.roomHUnits = roomHUnits;
    this.SprIDMap0 = new int[roomWUnits][roomHUnits]; this.ColMap0 = new boolean[roomWUnits][roomHUnits];
    for(int i = 0; i < SprIDMap0.length; i++) for(int j = 0; j < SprIDMap0[i].length; j++) SprIDMap0[i][j] = -1;
    //for(int i = 0; i < ColMap0.length; i++) for(int j = 0; j < ColMap0[i].length; j++) ColMap0[i][j] = true;  
    for(int i = 0; i < DatPaths.length; i++){ 
      SprDatPaths.add(DatPaths[i]); SprM0Sprites.add(loadImage(DatPaths[i]));
    }
  
      Players.add(Player1);
    //Players.add(Player2);
    for(int i = 4; i < 6; i++){
       Enemies.add(new F_FHaus(-3000-(initWidth*-i), GROUNDHEIGHT -i*50, inputCharPl2, true, true));
       Enemies.add(new F_PHaus(3000+initWidth*+i, GROUNDHEIGHT -i*50, inputCharPl2, true, true));
        Enemies.add(new F_KEnem(initWidth*i, GROUNDHEIGHT -i*50, inputCharPl2, false, true));
        //Enemies.add(new F_HHaus(initWidth/4*-i, GROUNDHEIGHT -i*50, inputCharPl2));
      //Enemies.add(new F_WHaus(initWidth/5*-i, GROUNDHEIGHT -i*50, inputCharPl2));
      Enemies.add( new F_DEnem(initWidth*-i, GROUNDHEIGHT , inputCharPl2, false, true) );
    }
   //Enemies.add(new F_Enemy0(initWidth/2, GROUNDHEIGHT , inputCharPl2, rootFData[0] ));
    for(Fighter E : Enemies){ E.setup(); }
    
    for(int i = 1; i < 6; i++){
      
      ColEbene.add(new ColRect(-200 - 500*i, GROUNDHEIGHT - 50, 0, 0,  20*i, 20*i));
      ColEbene.add(new ColRect(400 + 500*i, GROUNDHEIGHT - 50, 0, 0,  20*i, 20*i));
      ColEbene.add(new ColRect(-3000, GROUNDHEIGHT, 0, 0,  10, 300));
      ColEbene.add(new ColRect(3400, GROUNDHEIGHT, 0, 0,  10, 300));
      ColEbene.add(new ColRect( -3000, GROUNDHEIGHT-10, 0, 0,  6000, 10));
      ColEbene.add(new ColRect( -3000, GROUNDHEIGHT-800, 0, 0,  6000, 10));
      ColEbene.add(new ColRect( -4000, GROUNDHEIGHT-300, 0, 0,  1500, 10));
      ColEbene.add(new ColRect( 60*i, GROUNDHEIGHT-100 , 0, 0,  60, 60));
    }
    
  }
  
  void drawM0(int xstart, int xend, int ystart, int yend){ 
    imageMode(CORNER);
     fill(0, 70);   
        for(int i = xstart; i < xend && i >= 0 && i < ColMap0.length; i++){
      for(int j = ystart; j < yend && j >= 0 && j < ColMap0[i].length; j++){
        if(ColMap0[i][j]) rect((i-roomWUnits/2)*cellsize, (j-roomHUnits/2)*cellsize-cellsize, cellsize, cellsize);
      }}
    //println(xstart+":"+xend+":"+ystart+":"+yend);
    int srcX = -(roomWUnits/2*cellsize); int srcY = -(roomHUnits/2*cellsize);
    for(int i = xstart; i < xend && i >= 0 && 
    i < SprIDMap0.length; i++){
      for(int j = ystart; j < yend && j >= 0 && 
      j < SprIDMap0[i].length; j++){
        
        if(SprIDMap0[i][j] >= SprM0Sprites.size() || SprIDMap0[i][j] < 0 ) continue;
        if(SprM0Sprites.get(SprIDMap0[i][j]) == null || SprIDMap0[i][j] == -1){ println("nothin here"); 
      continue;}
        image(SprM0Sprites.get(SprIDMap0[i][j]), srcX+cellsize*i, srcY+cellsize*j);
      }
    }

  }
  
  boolean Edit = false; int xEditPos = 0, yEditPos = 0, itemstate = 0;
  EintragBox ColWidth = new EintragBox(0, 50, 200, 60, 2000, "100");
  EintragBox ColHeight = new EintragBox(0, 110, 200, 60, 2000, "100");
  
  void LevelEditor(){
    if(keyPressed && key == ' '){xEditPos += mouseX - pmouseX; yEditPos += mouseY - pmouseY;}
    
    camera(xEditPos+initWidth/2.0, yEditPos+initHeight/2.0, (initHeight/2.0) / tan(PI*30.0 / 180.0),
    xEditPos+initWidth/2.0, yEditPos+initHeight/2.0, 0,
    0, 1, 0);  
    drawM0((xEditPos/60)+roomWUnits/2, (xEditPos+initWidth)/60+roomWUnits/2, 
    (yEditPos/60)+roomHUnits/2, (yEditPos+initHeight)/60+roomHUnits/2);
    //drawM0(int(Camerabox.x-Camerabox.br/2)/cellsize, int(Camerabox.y-Camerabox.ho)/cellsize, int(Camerabox.x+Camerabox.br/2)/cellsize, int(Camerabox.y)/cellsize );
    for(ColRect c : ColEbene) c.draw();
    for(Fighter F : Enemies) F.draw();
    pushMatrix();
    translate(xEditPos, yEditPos);
    ColWidth.draw(); ColHeight.draw();
    fill(0); textSize(10);
    text(300, 100, itemstate);
    popMatrix();
    switch(itemstate){
    case 0: break;
    case 1: ColEbeneEditor(); break;
    case 2: EnemyEditor(); break;
    case 3: SprM0Editor((xEditPos/60)+roomWUnits/2, (xEditPos+initWidth)/60+roomWUnits/2, 
    (yEditPos/60)+roomHUnits/2, (yEditPos+initHeight)/60+roomHUnits/2); break;
    case 4: ColEbeneEditor2((xEditPos/60)+roomWUnits/2, (xEditPos+initWidth)/60+roomWUnits/2, 
    (yEditPos/60)+roomHUnits/2, (yEditPos+initHeight)/60+roomHUnits/2); break;
    }
  }
  void LEkeyReleased(){ ColWidth.keyReleased(); ColHeight.keyReleased();
    if(keyCode == UP) itemstate++; else if(keyCode == DOWN) itemstate--;
    println(itemstate);
  }
  void EnemyEditor(){
    if(click && mouseButton == LEFT){ 
      Fighter l_F = chooseFighter(int(ColWidth.boxText), xEditPos+mouseX, yEditPos+mouseY, inputCharPl2); 
      if(l_F != null ){l_F.setup(); l_F.AI_Controlled = true; Enemies.add(l_F);} }
    else if(click && mouseButton == RIGHT){} 
  }
  void ColEbeneEditor(){
    //println(xEditPos + " " + yEditPos + " " + mouseX + " " + mouseY);
    if(click && mouseButton == LEFT) ColEbene.add(new ColRect(xEditPos+mouseX, yEditPos+mouseY, 0, 0, int(ColWidth.boxText), int(ColHeight.boxText) ) );
    else
    if(click && mouseButton == RIGHT){ 
      
      for(int i = ColEbene.size()-1; i >= 0; i--){ 
        ColRect c = ColEbene.get(i);
        if(recPointCol(xEditPos+mouseX, yEditPos+mouseY, c.x, c.y- c.ho, c.br, c.ho)){ ColEbene.remove(i); break;}
      }
      
    }
    
  }
    void ColEbeneEditor2(int xstart, int xend, int ystart, int yend){
    fill(0);
    for(int i = xstart; i < xend && i >= 0 && i < ColMap0.length; i++){
      for(int j = ystart; j < yend && j >= 0 && j < ColMap0[i].length; j++){
            if(ColMap0[i][j]) rect(cellsize*i-roomWUnits/2, cellsize*j-roomHUnits/2 - cellsize, cellsize, cellsize);
        
            if(mousePressed && mouseButton == LEFT && recPointCol( (xEditPos + mouseX)+ roomWUnits/2*60, (yEditPos +mouseY) + roomHUnits/2*60, 
            cellsize*i, cellsize*j, cellsize, cellsize ) ){ ColMap0[i][j] = true; print("click"); return;}
    else
    if(mousePressed && mouseButton == RIGHT && recPointCol( (xEditPos + mouseX)+ roomWUnits/2*60, (yEditPos +mouseY) + roomHUnits/2*60, 
            cellsize*i, cellsize*j, cellsize, cellsize  ) ){ ColMap0[i][j] = false; return;}
        
      }
    }
    
  }
  
  void SprM0Editor(int xstart, int xend, int ystart, int yend){
       // int srcX = -(roomWUnits/2*cellsize); int srcY = -(roomHUnits/2*cellsize);
       imageMode(CORNER);
       if(int(ColWidth.boxText) < SprM0Sprites.size() && int(ColWidth.boxText) >= 0 )image(SprM0Sprites.get(int(ColWidth.boxText)), xEditPos, yEditPos);
       println((xEditPos + mouseX)+ roomWUnits/2*60, (yEditPos +mouseY) + roomHUnits/2*60);
    for(int i = xstart; i < xend && i >= 0 && i < SprIDMap0.length; i++){
      for(int j = ystart; j < yend && j >= 0 && j < SprIDMap0[i].length; j++){

        
            if(mousePressed && mouseButton == LEFT && recPointCol( (xEditPos + mouseX)+ roomWUnits/2*60, (yEditPos +mouseY) + roomHUnits/2*60, 
            cellsize*i, cellsize*j, cellsize, cellsize ) ){ SprIDMap0[i][j] = int(ColWidth.boxText); print("click"); return;}
    else
    if(mousePressed && mouseButton == RIGHT && recPointCol( (xEditPos + mouseX)+ roomWUnits/2*60, (yEditPos +mouseY) + roomHUnits/2*60, 
            cellsize*i, cellsize*j, cellsize, cellsize  ) ){ SprIDMap0[i][j] = -1; return;}
        
        
      }
    }
  }
  
  void loadRoom(){}
  void saveRoom(){}
  
  
    float[] colCheckEnv2(Fighter F, int xstart, int xend, int ystart, int yend){
    float maxAllowedFx = F.Force.x, l_temp = maxAllowedFx;
    float maxAllowedFy = F.Force.y, l_tempy = maxAllowedFy;
    ColRect cf = F.CollisionBox;
    cf.setColBools(false);
    ColRect ce = new ColRect(0, 0, 0, 0, cellsize, cellsize);
for(int i = xstart; i < xend && i >= 0 && i < ColMap0.length; i++){
  for(int j = ystart; j < yend && j >= 0 && j < ColMap0[i].length; j++){
      if(!ColMap0[i][j]) continue;
       //ce.x = cellsize * (i-roomWUnits/2); ce.y = cellsize * (j-roomHUnits/2); ce.br = cellsize; ce.ho = cellsize;
       ce = new ColRect((i-roomWUnits/2)*cellsize, (j-roomHUnits/2)*cellsize, 0, 0, cellsize, cellsize-1);
      ce.setColBools(false);
      F.CollisionBox.colCheckRect2(F, ce);
      
      if(cf.bottom && ce.top){
        maxAllowedFy = dist(ce.y - ce.ho, 0, F.y, 0);
      }
      if(cf.top && ce.bottom){
        maxAllowedFy = (dist(ce.y, 0, F.y - cf.ho, 0))*-1;
      }
      if(cf.lside && ce.rside){
        maxAllowedFx = (dist(ce.x + ce.br, 0, F.x - cf.br/2, 0))*-1;
      }
      if(cf.rside && ce.lside){
        maxAllowedFx = dist(ce.x, 0, F.x + cf.br/2, 0);
      }
      
      if(abs(maxAllowedFx) < abs(l_temp) ){
        l_temp = maxAllowedFx;
      }
      if(abs(maxAllowedFy) < abs(l_tempy) ){
        l_tempy = maxAllowedFy;
      }      
    
  }}
    //println(l_temp, l_tempy);
    return new float[]{l_temp, l_tempy};
  }
  
  float[] colCheckEnv(Fighter F){
    float maxAllowedFx = F.Force.x, l_temp = maxAllowedFx;
    float maxAllowedFy = F.Force.y, l_tempy = maxAllowedFy;
    ColRect cf = F.CollisionBox;
    cf.setColBools(false);
    for(ColRect ce : ColEbene){
      ce.setColBools(false);
      F.CollisionBox.colCheckRect2(F, ce);
      
      if(cf.bottom && ce.top){
        maxAllowedFy = dist(ce.y - ce.ho, 0, F.y, 0);
        /*F.Force.y = 0; 
        F.gforce = 0;
        F.y = ce.y-ce.ho;*/
      }
      if(cf.top && ce.bottom){
        maxAllowedFy = (dist(ce.y, 0, F.y - cf.ho, 0))*-1;
      }
      if(cf.lside && ce.rside){
        maxAllowedFx = (dist(ce.x + ce.br, 0, F.x - cf.br/2, 0))*-1;
      }
      if(cf.rside && ce.lside){
        maxAllowedFx = dist(ce.x, 0, F.x + cf.br/2, 0);
      }
      
      if(abs(maxAllowedFx) < abs(l_temp) ){
        l_temp = maxAllowedFx;
      }
      if(abs(maxAllowedFy) < abs(l_tempy) ){
        l_tempy = maxAllowedFy;
      }
      
    }
    
    return new float[]{l_temp, l_tempy};
  }
  
    void keyPressed(){   for(Fighter P : Players){ P.keyPressed(); } 
  for(Fighter E : Enemies){ E.keyPressed(); }
}
  void keyReleased(){  for(Fighter P : Players){ P.keyReleased(); } 
  for(Fighter E : Enemies){ E.keyReleased(); }
  if(key == 'B' && !this.Edit){ this.Edit = true; println("Editmode on");} else if(key == 'B' && this.Edit == true){ this.Edit = false; println("Editmode off");}
  if(Edit){LEkeyReleased();}
}
  
}

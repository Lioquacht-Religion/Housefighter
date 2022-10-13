class Gamestate{
  boolean deletMe = false;
  void setup(){}
  void gameLogic(){}
  void drawVisuals(){}
  void keyPressed(){}
  void keyReleased(){}
  void finishState(){};
}

class MainMenu extends Gamestate{
  MenuBox[] L1; MenuBox[][] MBGrid;
  GridCursor GCursor;
  
  MainMenu(){
    L1 = new MenuBox[]{new MenuBox(initWidth/2, initHeight/2, initWidth/4, initHeight/16, "Singleplayer", color(255)),
    new MenuBox(initWidth/2, initHeight/2 + (initHeight/10*1), initWidth/4, initHeight/16, "Multiplayer", color(255)),
    new MenuBox(initWidth/2, initHeight/2 + (initHeight/10*2), initWidth/4, initHeight/16, "Training", color(255)),
    new MenuBox(initWidth/2, initHeight/2 + (initHeight/10*3), initWidth/4, initHeight/16, "Options", color(255)),
  };
    MBGrid = new MenuBox[][]{ {L1[0]}, {L1[1]}, {L1[2]}, {L1[3]} };
    GCursor = new GridCursor(MBGrid, 0, 0, Con1, 4, 6);
  }
  
  void setup(){}
  
  void gameLogic(){
    camera();
    showInputs(Con1, 100, 250);
    Con1.deviceInput(); GCursor.Logic(); 
    Con1.draw();  GCursor.draw(0, 0);
  
  if(L1[0].clicked)
    CurGameState = new F_selectScreen(new Singleplayer());
  else if(L1[1].clicked)
    CurGameState = new F_selectScreen(new StageSelect( new F_2V2Multiplayer() ) );
  else if(L1[2].clicked)
    CurGameState = new F_selectScreen(new StageSelect( new TrainingMode() ) );
  else if(L1[3].clicked){
        //  CurGameStates.add(new F_2V2Fightmode());    
  }
  
  for(MenuBox m : L1){
    m.draw();
  }
  
  }
  
  void drawVisuals(){}
  void keyPressed(){}
  void keyReleased(){}
}

class StageSelect extends Gamestate{
  int CurStageID = 0; Gamestate NextGS;
  MenuBox LeftBox = new MenuBox(initWidth/4-25, initHeight/2, 50, 200, "<", 180), RightBox = new MenuBox(initWidth-initWidth/4-25, initHeight/2, 50, 200, ">", 180),
  ChUrNeighBox = new MenuBox(50, 40, 800, 80, "CHOOSE YOUR NEIGHBOURHOOD!!", 255);
  Stage[] StageList = {new TrainingStage(), new BergGrossStage()};
  StageSelect(Gamestate NextGS){ this.NextGS = NextGS; }
  void gameLogic(){
    if(LeftBox.clicked && CurStageID > 0)CurStageID--; if(RightBox.clicked && CurStageID < StageList.length-1) CurStageID++;
    StageBackground = StageList[CurStageID];
    if(ChUrNeighBox.clicked) CurGameState = NextGS;
  }
  void drawVisuals(){
        camera(0+initWidth/2.0, initHeight/2.0, (initHeight/2.0) / tan(PI*30.0 / 180.0),
    0+initWidth/2.0, initHeight/2.0, 0,
    0, 1, 0);
    StageList[CurStageID].drawBackground();
    StageList[CurStageID].drawForeground();
    LeftBox.draw(); RightBox.draw(); ChUrNeighBox.draw();
  }
  void keyPressed(){}
  void keyReleased(){}
  
}

class Singleplayer extends Gamestate{
  Room Rtest; boolean menuOn = false; MP_Menu Menu = new MP_Menu();
  
  /*Fighter[] rootFData = {new F_PHaus(initWidth*2, GROUNDHEIGHT, inputCharPl2, true, false), 
  new F_Enemy0(initWidth/2, GROUNDHEIGHT , inputCharPl2, true, true)
  ,new F_Enemy1(initWidth/2, GROUNDHEIGHT , inputCharPl2, true, true)
  //,new F_HHaus(initWidth*3, GROUNDHEIGHT, inputCharPl2, true, false),
  //new F_WHaus(initWidth, GROUNDHEIGHT, inputCharPl2, true, true)
};*/
  
  
  Singleplayer(){
     Rtest = new Room(100, 50, "SP_Spr/GroundM0.png", "SP_Spr/GroundL0.png", "SP_Spr/GroundR0.png", 
     "SP_Spr/RockBM0.png", "SP_Spr/RockLB0.png", "SP_Spr/RockLT0.png", "SP_Spr/RockMT0.png", "SP_Spr/RockRB0.png", "SP_Spr/RockRT0.png", 
     "SP_Spr/PlatL0.png", "SP_Spr/PlatM0.png", "SP_Spr/PlatR0.png");
  }
  
  void setup(){}
  
  void menu(Fighter P1, Fighter P2){
    Menu.draw(P1, P2);
    if(Menu.Backbox.clicked) menuOn = false;
  }
  
  void gameLogic(){
    if(menuOn && !Rtest.Edit){ Menu.inGameEffect(Player1, Player2); return;}  

    if(Rtest.Edit)Rtest.LevelEditor();
    if(Rtest.Edit)return;
    
    for(Fighter P : Rtest.Players){
      P.CollisionBox.x = P.x - P.CollisionBox.br/2;  P.CollisionBox.y = P.y; 
        //Rtest.colCheckEnv(P);
        Rtest.colCheckEnv2(P,int(P.x-initWidth)/60+Rtest.roomWUnits/2, int(P.x+initWidth)/60+Rtest.roomWUnits/2, 
        int(P.y-initHeight)/60+Rtest.roomHUnits/2, int(P.y+initHeight/2)/60+Rtest.roomHUnits/2);
        P.gameLogic(P);
        P.manualFacing();
   P.CollisionBox.x = P.x - P.CollisionBox.br/2;  P.CollisionBox.y = P.y; 
   
   //float[] posf = Rtest.colCheckEnv2(P, 0, 0, Rtest.roomWUnits, Rtest.roomHUnits);
   //Rtest.colCheckEnv2(P,int(Player1.x-initWidth)/60+Rtest.roomWUnits/2, int(Player1.x+initWidth)/60+Rtest.roomWUnits/2, 
    //int(Player1.y-initHeight)/60+Rtest.roomHUnits/2, int(Player1.y+initHeight/2)/60+Rtest.roomHUnits/2);
   /*float[] posf2 = Rtest.colCheckEnv(P), posf3 = new float[2];
   if(posf[0] <= posf2[0]) posf3[0] = posf[0]; else posf3[0] = posf2[0];
   if(posf[1] <= posf2[1]) posf3[1] = posf[1]; else posf3[1] = posf2[1];*/
   P.updatePos(Rtest.colCheckEnv2(P,int(P.x-initWidth)/60+Rtest.roomWUnits/2, int(P.x+initWidth)/60+Rtest.roomWUnits/2, 
    int(P.y-initHeight)/60+Rtest.roomHUnits/2, int(P.y+initHeight/2)/60+Rtest.roomHUnits/2));
    //P.updatePos(Rtest.colCheckEnv(P));
    }
    
    for(int i = Rtest.Enemies.size()-1; i >= 0; i--){
      Fighter F = Rtest.Enemies.get(i);
      if(F.curHP <= 0){
        for(int j = 0; j < 8; j++){
          Soundeffects[1].cue(0);
          Soundeffects[1].play();
          VisEffectsList.add(new VisualEffect(F.x + random(-100, 100), F.y-100 + random(-100, 100), RCEff, 0));
        }
        slowMoDur = 12; slowMoValue = 2;
      Rtest.Enemies.remove(i);
     }
    }
    
   for(Fighter F : Rtest.Enemies){
     if(F.x > Camerabox.x - Camerabox.br && F.x < Camerabox.x + Camerabox.br){
     
     
     for(Fighter P : Rtest.Players){
    ColCircle[] l_FBoxes = F.checkHit(P);
    ColCircle[] l_P1Boxes = P.checkHit(F);
    
    Action l_P1Act = P.operationsOnHit(F, l_P1Boxes);
    Action l_F1Act = F.operationsOnHit(P, l_FBoxes);
     
       
        if(l_FBoxes != null){
    P.CurAction.reset();
    P.CurAction = l_F1Act;
    }
    if(l_P1Boxes != null){
    F.CurAction.reset();
    F.CurAction = l_P1Act;
    }
   
     
        P.CollisionBox.x = P.x - P.CollisionBox.br/2;  P.CollisionBox.y = P.y; 
        F.CollisionBox.x = F.x - F.CollisionBox.br/2;  F.CollisionBox.y = F.y;
       P.CollisionBox.colCheckRect( P, F );   
       F.CollisionBox.colCheckRect( F, P ); 
        
         Fighter Pl1 = P, Pl2 = F;
    ColRect Cr1 = P.CollisionBox, Cr2 = F.CollisionBox;
    
    if( (Cr1.lside && Cr2.rside || Cr2.lside && Cr1.rside) && Pl1.dirMult != Pl2.dirMult){
      //float lf = Pl1.Force.x; Pl1.Force.x = Pl2.Force.x; Pl2.Force.x = lf;
      //float x1 = Pl2.x + (Pl1.CollisionBox.br/2 + Pl2.CollisionBox.br/2 +1) * (Pl1.dirMult * -1);
      //float x2 = Pl1.x + (Pl2.CollisionBox.br/2 + Pl1.CollisionBox.br/2 +1) * (Pl2.dirMult * -1);
      //Pl1.x = x1;
      //Pl2.x = x2;
    }
    else     if( (Cr1.lside && Cr2.rside || Cr2.lside && Cr1.rside) && Pl1.x <= Pl2.x){
      //Pl1.Force.x = 0; Pl2.Force.x = 0;
      //float lf = Pl1.Force.x; Pl1.Force.x = Pl2.Force.x; Pl2.Force.x = lf;
      //float x1 = Pl2.x - (Pl1.CollisionBox.br/2 + Pl2.CollisionBox.br/2 +1);
      //float x2 = Pl1.x + (Pl2.CollisionBox.br/2 + Pl1.CollisionBox.br/2 +1);
      //Pl1.x = x1;
      //Pl2.x = x2;
    }
    else     if( (Cr1.lside && Cr2.rside || Cr2.lside && Cr1.rside) && Pl1.x > Pl2.x){
      //Pl1.Force.x = 0; Pl2.Force.x = 0;
      //float lf = Pl1.Force.x; Pl1.Force.x = Pl2.Force.x; Pl2.Force.x = lf;
      //float x1 = Pl2.x + (Pl1.CollisionBox.br/2 + Pl2.CollisionBox.br/2 +1);
      //float x2 = Pl1.x - (Pl2.CollisionBox.br/2 + Pl1.CollisionBox.br/2 +1);
      //Pl1.x = x1;
      //Pl2.x = x2;
    }
    
    
            P.CollisionBox.x = P.x - P.CollisionBox.br/2;  P.CollisionBox.y = P.y; 
        F.CollisionBox.x = F.x - F.CollisionBox.br/2;  F.CollisionBox.y = F.y;
        P.CollisionBox.colCheckRect( P, F );   
        F.CollisionBox.colCheckRect( F, P ); 
        

    
  if(Cr1.top || Cr1.bottom){
    //Pl1.Force.y = 0; 
  }
  if(Cr2.top || Cr2.bottom){
   // Pl2.Force.y = 0;
  }
  
       F.CollisionBox.x = F.x - F.CollisionBox.br/2;  F.CollisionBox.y = F.y;
     Rtest.colCheckEnv2(F, int(F.x-initWidth)/60+Rtest.roomWUnits/2, int(F.x+initWidth)/60+Rtest.roomWUnits/2, 
    int(F.y-initHeight)/60+Rtest.roomHUnits/2, int(F.y+initHeight/2)/60+Rtest.roomHUnits/2);
     F.gameLogic(Player1);
     F.facingCheckAndChange(Player1);
     F.CollisionBox.x = F.x - F.CollisionBox.br/2;  F.CollisionBox.y = F.y;
       F.updatePos(Rtest.colCheckEnv2(F, int(F.x-initWidth)/60+Rtest.roomWUnits/2, int(F.x+initWidth)/60+Rtest.roomWUnits/2, 
    int(F.y-initHeight)/60+Rtest.roomHUnits/2, int(F.y+initHeight/2)/60+Rtest.roomHUnits/2));
  
   }  
   
     }
     
   }
  
  for(int i = VisEffectsList.size()-1; i >= 0; i--){
    if(VisEffectsList.get(i).exTimer <= 0){
      VisEffectsList.remove(i);
    }
  }
     
  }
  
      void drawVisuals(){
        if(Rtest.Edit) return;
        if(frameFreeze <= 0){
  //centerX = initWidth/2.0;//dist(Player1.x, 0, Player2.x, 0)/2;
  float centerZ = 0;
  float centerY = 0;
  float centerZ2 = centerZ / tan(PI*30.0 / 180.0);
  
   Camerabox.x = Player1.x + (initWidth/16) * Player1.dirMult;
   centerY = (initHeight - initHeight/4 - Player1.y);
  
   camera(Camerabox.x, initHeight/2.0 - centerY, (initHeight/2.0+centerZ) / tan(PI*30.0 / 180.0),
    Camerabox.x, initHeight/2.0 - centerY, 0,
    0, 1, 0);
    
  background(150);
    //StageBackground.drawBackground();
    Rtest.drawM0(int(Player1.x-initWidth)/60+Rtest.roomWUnits/2, int(Player1.x+initWidth)/60+Rtest.roomWUnits/2, 
    int(Player1.y-initHeight)/60+Rtest.roomHUnits/2, int(Player1.y+initHeight/2)/60+Rtest.roomHUnits/2);
    for(ColRect ce : Rtest.ColEbene){
      ce.draw();
    }
    
    fill(0);
    textSize(30);
    
    if(Player1.comboCount > 0){ text("COMBO:" + Player1.comboCount, int(Camerabox.x) - initWidth/2, 200 - int(centerY)); }
    //if(Player2.counterState){ text("COUNTER", int(Camerabox.x) - initWidth/2, 150 - int(centerY)); }
    //if( Player2.comboCount > 0){ text("COMBO:" + Player2.comboCount, int(Camerabox.x) + initWidth/4, 200 - int(centerY)); }
    if(Player1.counterState){ text("COUNTER", int(Camerabox.x) + initWidth/4, 150 - int(centerY)); }
    
  for(Fighter F : Rtest.Enemies){
     if(F.x > Camerabox.x - Camerabox.br/2 && F.x < Camerabox.x + Camerabox.br/2){
       F.draw();
     }
  }
  
  for(Fighter P : Rtest.Players){
    P.draw();
  }
  
    line(-initWidth, GROUNDHEIGHT, initWidth+initWidth/2, GROUNDHEIGHT);
  for(int i = VisEffectsList.size()-1; i >= 0; i--){
     VisEffectsList.get(i).draw();
  }
 
  StageBackground.drawForeground();
  
  Player1.drawBars(1);
        
  fill(0);
  textSize(20);
  text("fps: "+frameRate, Camerabox.x, initHeight/2);
  if(menuOn && !Rtest.Edit){camera(); Camerabox.x = Camerabox.br/2; menu(Player1, Player2);}
      }
    else{ frameFreeze--;}
    
  }
  
  void keyPressed(){   Rtest.keyPressed(); }
  void keyReleased(){  Rtest.keyReleased(); 
            if(key == ' ' && !menuOn && !Rtest.Edit) menuOn = true;
    else if(key == ' ' && menuOn && !Rtest.Edit) menuOn = false;

}
  
}

class GS_Handler extends Gamestate{
  ArrayList<Gamestate> ToHandle;
  GS_Handler(ArrayList<Gamestate> ToHandle){
    this.ToHandle = ToHandle;
  }
    void gameLogic(){
      for(int i = ToHandle.size()-1; 0 <= i; i--){
        if(ToHandle.get(i).deletMe){ ToHandle.remove(i);}
      }
    }
  void drawVisuals(){ text("Only GS_Hadler loaded", initWidth/2, initHeight/2);}
}

class F_selectScreen extends Gamestate{ 
  Gamestate StartPressGS = null;
  GridCursor<CharSelectKachel> GCursor, GCursor2;
  CharSelectKachel[][] charBoxs;
  MenuBox BackBox = new MenuBox(initWidth/2, initHeight/2+100, 80, 50, "BACK", color(255, 0, 0) ),
  StartBox = new MenuBox(initWidth/2, initHeight/2+160, 100, 70, "START", color(0, 255, 0) );
  
  F_selectScreen(Gamestate StartPressGS){ 
     this.StartPressGS = StartPressGS;
     int[] tmp = {30, 30}; String tmp_dp = "UI-Assets/SelectKacheln/", tmp_dp2 = "UI-Assets/CharPortraits/CharPortrait";
     
     charBoxs = new CharSelectKachel[][]{ 
       {
         new CharSelectKachel(250, 140, 100, 100, loadImage(tmp_dp+"selectkachel0.png"), new Animation(tmp, 0, 2, tmp_dp+"sk_FH_Sprs"), loadImage(tmp_dp2+"0.png") ),
         new CharSelectKachel(350, 140, 100, 100, loadImage(tmp_dp+"selectkachel1.png"), new Animation(tmp, 0, 2, tmp_dp+"sk_HH_Sprs"), loadImage(tmp_dp2+"1.png") ),
         new CharSelectKachel(450, 140, 100, 100, loadImage(tmp_dp+"selectkachel2.png"), new Animation(tmp, 0, 2, tmp_dp+"sk_WH_Sprs"), loadImage(tmp_dp2+"2.png") ),
         new CharSelectKachel(550, 140, 100, 100, loadImage(tmp_dp+"selectkachel0.png"), new Animation(tmp, 0, 2, tmp_dp+"sk_FH_Sprs"), loadImage(tmp_dp2+"3.png") )
     }, 
       {
         new CharSelectKachel(250, 220, 100, 100, loadImage(tmp_dp+"selectkachel0.png"), new Animation(tmp, 0, 2, tmp_dp+"sk_FH_Sprs"), loadImage(tmp_dp2+"4.png") ),
         new CharSelectKachel(350, 220, 100, 100, loadImage(tmp_dp+"selectkachel1.png"), new Animation(tmp, 0, 2, tmp_dp+"sk_HH_Sprs"), loadImage(tmp_dp2+"1.png") ),
         new CharSelectKachel(450, 220, 100, 100, loadImage(tmp_dp+"selectkachel2.png"), new Animation(tmp, 0, 2, tmp_dp+"sk_WH_Sprs"), loadImage(tmp_dp2+"2.png") ),
         new CharSelectKachel(550, 220, 100, 100, loadImage(tmp_dp+"selectkachel0.png"), new Animation(tmp, 0, 2, tmp_dp+"sk_FH_Sprs"), loadImage(tmp_dp2+"3.png") )
     },
       {
         new CharSelectKachel(250, 300, 100, 100, loadImage(tmp_dp+"selectkachel0.png"), new Animation(tmp, 0, 2, tmp_dp+"sk_FH_Sprs"), loadImage(tmp_dp2+"4.png") )
     }
     };
     
     GCursor = new GridCursor<CharSelectKachel>(charBoxs, 0, 0, Con1, 4, 6);
     GCursor2 = new GridCursor<CharSelectKachel>(charBoxs, 3, 0, Con2, 4, 6);
   }
   
  void gameLogic(){
          camera(initWidth/2, initHeight/2.0, (initHeight/2.0) / tan(PI*30.0 / 180.0),
    initWidth/2, initHeight/2.0, 0,
    0, 1, 0);
  background(180, 0, 255);
  
    if(BackBox.clicked){
      if(Player2 != null){
        Player2 = null;
      }
      else if(Player1 != null){
        Player1 = null;
      }
  }
  
  if(StartBox.clicked && Player1 != null && Player2 != null){
    Player1.setup();
    if(device1 != null){
    Player1.device = device1;
    Player1.setConDevice();
    }
    Player2.setup();
    if(device2 != null){
    Player2.device = device2;
    Player2.setConDevice();
    }
    //CurGameStates.add(new F_2V2Fightmode());
    CurGameState = StartPressGS;
    deletMe = true;
  }
  
    showInputs(Con1, 100, 250);
    Con1.deviceInput(); GCursor.Logic(); 
    Con1.draw();  GCursor.draw(0, 0);
      imageMode(CENTER);
      image(GCursor.CurMB().BigCharArt, initWidth/4, initHeight/2);
    
    showInputs(Con2, 700, 250);
    Con2.deviceInput(); GCursor2.Logic();
    Con2.draw();  GCursor2.draw(0, 0);
      image(GCursor2.CurMB().BigCharArt, initWidth-initWidth/4, initHeight/2);
      
    if(GCursor.clicked){
      if(Player1 == null) 
      Player1 = chooseFighter( 4*GCursor.yGrid+GCursor.xGrid, initWidth/4, GROUNDHEIGHT, inputCharPl1);
    }else if(GCursor.backClick) Player1 = null;
       
    if(GCursor2.clicked){
      if(Player2 == null) 
      Player2 = chooseFighter( 4*GCursor2.yGrid+GCursor2.xGrid, initWidth-initWidth/4, GROUNDHEIGHT, inputCharPl2);
    }else if(GCursor2.backClick) Player2 = null;
  
  StartBox.draw();
  BackBox.draw();
  
  for(int i = 0; i < charBoxs.length; i++){
    for(int j = 0; j < charBoxs[i].length; j++){
      CharSelectKachel l_cb = charBoxs[i][j];
      if(l_cb == null) continue;
    charBoxs[i][j].draw();
    if(l_cb.mouseInBox()){
      imageMode(CENTER);
      image(l_cb.BigCharArt, initWidth/4, initHeight/2);
    }
    if(l_cb.clicked){
      if(Player1 == null){
        Player1 = chooseFighter( 4*i+j, initWidth/4, GROUNDHEIGHT, inputCharPl1);
       
      }
      else if(Player2 == null){
        Player2 = chooseFighter( 4*i+j, initWidth-initWidth/4, GROUNDHEIGHT, inputCharPl2);
        
      }
    }
    }

  }
  
    imageMode(CENTER);
 if(Player1 != null){Player1.CurAction = Player1.Standing; Player1.CurAction.AttAnim.handleAnim(); image(Player1.CurAction.AttAnim.Sprites[Player1.CurAction.AttAnim.curCollumn],
 initWidth/4, initHeight/2);}
 if(Player2 != null){Player2.CurAction = Player2.Standing; Player2.CurAction.AttAnim.handleAnim(); image(Player2.CurAction.AttAnim.Sprites[Player2.CurAction.AttAnim.curCollumn], 
 initWidth-initWidth/4, initHeight/2);}
  
  }
  void drawVisuals(){}
  
  void keyPressed(){}
  void keyReleased(){}
  
}

class TrainingMode extends Gamestate{
  Training_Menu Menu = new Training_Menu(); boolean menuOn = false;
  Gamestate CurGS = new F_2V2Fightmode();
    void setup(){}
  void gameLogic(){
    if(//menuOn && 
  CurGS instanceof F_2V2Fightmode){ Menu.inGameEffect(Player1, Player2); if(menuOn){return;} }  
    CurGS.gameLogic();
  }
  void drawVisuals(){
    CurGS.drawVisuals();
    if(menuOn) menu(Player1, Player2);
  }
  void keyPressed(){CurGS.keyPressed();}
  void keyReleased(){
    CurGS.keyReleased();
    if(key == ' ' && CurGS instanceof F_2V2Fightmode && !menuOn) menuOn = true;
    else if(key == ' ' && CurGS instanceof F_2V2Fightmode && menuOn) menuOn = false;
      
    if(key == 'c' && CurGS instanceof F_2V2Fightmode){
      F_Editmode g = new F_Editmode();
      g.EditFighter = Player1;
        g.EditFighter.HitBoxes.clear();
        g.EditFighter.HurtBoxes.clear();
        g.eModeTimer = 0;
        g.eModeCurCBox = 0;
        g.eModeCurCAnim = 0;
        g.EditDatnamBox.boxText = g.EditFighter.CurAction.datnam;
        delCurGSlist();
        //CurGameStates.add(g);
        CurGS = g;
  }
    if(key == 'v' && CurGS instanceof F_Editmode){
        finishState();
        //CurGameStates.add(new F_2V2Fightmode());
        CurGS = new F_2V2Fightmode();
  }
  
  if(key == 'B' && !Player2.AI_Controlled){
    Player2.AI_Controlled = true;
  }
  else  if(key == 'B' && Player2.AI_Controlled){
    Player2.AI_Controlled = false;
  }
  
  InputRecord r = Player1.Recorder; InputRecord r2 = Player2.Recorder;
  if(key == 'N'){ r.deleteRec(); r2.deleteRec(); println("deleteRec");}

}
  
  void menu(Fighter P1, Fighter P2){
    Menu.draw(P1, P2);
    if(Menu.Backbox.clicked) menuOn = false;
  }
  
}

class F_2V2Multiplayer extends Gamestate{
    Gamestate CurGS = new F_2V2Fightmode(); boolean menuOn = false; MP_Menu Menu = new MP_Menu();
    void setup(){}
  void gameLogic(){
        if(menuOn && CurGS instanceof F_2V2Fightmode){ Menu.inGameEffect(Player1, Player2); return;}  
    CurGS.gameLogic();
    if(Player1.curHP <= 0 || Player2.curHP <= 0) CurGameState = new F_Winscreen(this);
  }
  void drawVisuals(){
    CurGS.drawVisuals();
    if(menuOn) menu(Player1, Player2);
  }
  void keyPressed(){CurGS.keyPressed();}
  void keyReleased(){
    CurGS.keyReleased();
        if(key == ' ' && CurGS instanceof F_2V2Fightmode && !menuOn) menuOn = true;
    else if(key == ' ' && CurGS instanceof F_2V2Fightmode && menuOn) menuOn = false;
}
  
  void menu(Fighter P1, Fighter P2){
    Menu.draw(P1, P2);
    if(Menu.Backbox.clicked) menuOn = false;
  }
  
}

class F_2V2Fightmode extends Gamestate{
  
 // Fighter Player1, Player2;
  boolean searchDevices = false;
  
  void gameLogic(){
     
    if(searchDevices){
       /*control.getDevices().clear();
      println(control.deviceListToText("") );
      println("searching for devices now");
        
         Player1.device = null; 
           Player2.device = null; 
         control = ControlIO.getInstance(p);
         Player1.device = control.getMatchedDeviceSilent("HFcontroller"); 
         if(Player1.device != null) {println( Player1.device.getName()+ " " + Player1.device.getPortTypeName() ); 
       Player1.setConDevice(); println("connect");}
        Player2.device = control.getMatchedDeviceSilent("HFcontroller"); if(Player2.device != null) {println( Player2.device.getName() ); Player2.setConDevice(); println("connect");}
         
         println(control.deviceListToText("") );
         searchDevices =false;*/
         Player1.PlContr.searchDevice(); //Player2.PlContr.searchDevice();
         searchDevices = false;
    }

   Player1.gameLogic(Player2);
   Player2.gameLogic(Player1); 
   
    ColCircle[] l_Boxes2 = Player2.checkHit(Player1);
    ColCircle[] l_Boxes1 = Player1.checkHit(Player2);
    
    Action l_P1Act = Player1.operationsOnHit(Player2, l_Boxes1);
    Action l_P2Act = Player2.operationsOnHit(Player1, l_Boxes2);
    if(l_Boxes1 != null){
    Player2.changeAction(l_P1Act);
    //Player2.CurAction = l_P1Act;
    }
    if(l_Boxes2 != null){
    Player1.changeAction(l_P2Act);
    //Player1.CurAction = l_P2Act;
    }

    
    Player1.updatePos(new float[]{0, 0});
    Player2.updatePos(new float[]{0, 0});
    
  Player1.facingCheckAndChange(Player2);
  Player2.facingCheckAndChange(Player1);
  
  if(Player1.CurAction.collision && Player2.CurAction.collision){
        Player1.CollisionBox.x = Player1.x - Player1.CollisionBox.br/2;  Player1.CollisionBox.y = Player1.y; 
        Player2.CollisionBox.x = Player2.x - Player2.CollisionBox.br/2;  Player2.CollisionBox.y = Player2.y;
          Player1.CollisionBox.colCheckRect( Player1, Player2 );   
          Player2.CollisionBox.colCheckRect( Player2, Player1 ); 
        
    Fighter Pl1 = Player1, Pl2 = Player2;
    ColRect Cr1 = Player1.CollisionBox, Cr2 = Player2.CollisionBox;
    
    if( (Cr1.lside && Cr2.rside || Cr2.lside && Cr1.rside) && Pl1.dirMult != Pl2.dirMult){
      Pl1.Force.x = 0; Pl2.Force.x = 0;
      float x1 = Pl2.x + (Pl1.CollisionBox.br/2 + Pl2.CollisionBox.br/2 +1) * (Pl1.dirMult * -1);
      float x2 = Pl1.x + (Pl2.CollisionBox.br/2 + Pl1.CollisionBox.br/2 +1) * (Pl2.dirMult * -1);
      Pl1.x = x1;
      Pl2.x = x2;
    }
    else     if( (Cr1.lside && Cr2.rside || Cr2.lside && Cr1.rside) && Pl1.x <= Pl2.x){
      Pl1.Force.x = 0; Pl2.Force.x = 0;
      float x1 = Pl2.x - (Pl1.CollisionBox.br/2 + Pl2.CollisionBox.br/2 +1);
      float x2 = Pl1.x + (Pl2.CollisionBox.br/2 + Pl1.CollisionBox.br/2 +1);
      Pl1.x = x1;
      Pl2.x = x2;
    }
    else     if( (Cr1.lside && Cr2.rside || Cr2.lside && Cr1.rside) && Pl1.x > Pl2.x){
      Pl1.Force.x = 0; Pl2.Force.x = 0;
      float x1 = Pl2.x + (Pl1.CollisionBox.br/2 + Pl2.CollisionBox.br/2 +1);
      float x2 = Pl1.x - (Pl2.CollisionBox.br/2 + Pl1.CollisionBox.br/2 +1);
      Pl1.x = x1;
      Pl2.x = x2;
    }
    
            Player1.CollisionBox.x = Player1.x - Player1.CollisionBox.br/2;  Player1.CollisionBox.y = Player1.y; 
        Player2.CollisionBox.x = Player2.x - Player2.CollisionBox.br/2;  Player2.CollisionBox.y = Player2.y;
          Player1.CollisionBox.colCheckRect( Player1, Player2 );   
          Player2.CollisionBox.colCheckRect( Player2, Player1 ); 
    
  if(Cr1.top || Cr1.bottom){
    Pl1.Force.y = 0; 
  }
  if(Cr2.top || Cr2.bottom){
    Pl2.Force.y = 0;
  }
  
  }
  
         //Bug: siehe liste
  if(Camerabox.x - Camerabox.br/2 >= Player1.x - Player1.CollisionBox.br/2) 
  Player1.x = Camerabox.x - Camerabox.br/2+Player1.CollisionBox.br/2;
  if(Camerabox.x + Camerabox.br/2 <= Player1.x + Player1.CollisionBox.br/2) 
  Player1.x = Camerabox.x + Camerabox.br/2-Player1.CollisionBox.br/2;
  if(Camerabox.x - Camerabox.br/2 >= Player2.x - Player2.CollisionBox.br/2) 
  Player2.x = Camerabox.x - Camerabox.br/2+Player2.CollisionBox.br/2;
  if(Camerabox.x + Camerabox.br/2 <= Player2.x + Player2.CollisionBox.br/2) 
  Player2.x = Camerabox.x + Camerabox.br/2-Player2.CollisionBox.br/2;
      /*Player2.x = constrain(Player2.x, Camerabox.x - Camerabox.br/2 + 60, 
      Camerabox.x + Camerabox.br/2 - 60);
      Player1.x = constrain(Player1.x, Camerabox.x - Camerabox.br/2 + 60, 
      Camerabox.x + Camerabox.br/2 - 60);*/

           
  line(-initWidth, GROUNDHEIGHT, initWidth+initWidth/2, GROUNDHEIGHT);
  for(int i = VisEffectsList.size()-1; i >= 0; i--){
    if(VisEffectsList.get(i).exTimer <= 0){
      VisEffectsList.remove(i);
    }
  }

  }
  
  void drawVisuals(){
        if(frameFreeze <= 0){
  //centerX = initWidth/2.0;//dist(Player1.x, 0, Player2.x, 0)/2;
  float centerZ = 0;
  float centerY = 0;
  float centerZ2 = centerZ / tan(PI*30.0 / 180.0);
  
  if(Player1.x < Player2.x)
    Camerabox.x = Player1.x-Player1.CollisionBox.br/2 + 
    dist(Player1.x-Player1.CollisionBox.br/2, 0, Player2.x+Player2.CollisionBox.br/2, 0)/2; 
  else 
    //Camerabox.x = Player1.x - dist(Player1.x, 0, Player2.x, 0)/2;
        Camerabox.x = Player1.x+Player1.CollisionBox.br/2 - 
    dist(Player1.x+Player1.CollisionBox.br/2, 0, Player2.x-Player2.CollisionBox.br/2, 0)/2;
    
  if( Camerabox.ColRectLineVerCheck(-initWidth/2) ){
    Camerabox.x = 0;
  }
  else if( Camerabox.ColRectLineVerCheck(initWidth+initWidth/2) ){
    Camerabox.x = initWidth;
  }
  if((GROUNDHEIGHT - Player1.y) > initHeight - initHeight/4){
    centerY = (initHeight/2 - Player1.y);
  }
  if((GROUNDHEIGHT - Player2.y) > initHeight - initHeight/4){
    centerY = (initHeight/2 - Player2.y);
  }
  
  
    camera(Camerabox.x, initHeight/2.0 - centerY, (initHeight/2.0+centerZ) / tan(PI*30.0 / 180.0),
    Camerabox.x, initHeight/2.0 - centerY, 0,
    0, 1, 0);
    
  background(150);
    StageBackground.drawBackground();
    
    fill(0);
    textSize(30);
    
    if(Player1.comboCount > 0){ text("COMBO:" + Player1.comboCount, int(Camerabox.x) - initWidth/2, 200 - int(centerY)); }
    if(Player2.counterState){ text("COUNTER", int(Camerabox.x) - initWidth/2, 150 - int(centerY)); }
    if( Player2.comboCount > 0){ text("COMBO:" + Player2.comboCount, int(Camerabox.x) + initWidth/4, 200 - int(centerY)); }
    if(Player1.counterState){ text("COUNTER", int(Camerabox.x) + initWidth/4, 150 - int(centerY)); }
    pushMatrix();
    translate(0, 0, centerZ2);
    showInputbuffer(Player1, int(Camerabox.x) - initWidth/4, 20 - int(centerY));
    showInputbuffer(Player2, int(Camerabox.x) + initWidth/4, 20 - int(centerY));
    fill(0);
    rect(int(Camerabox.x) - initWidth/2 - 1, -centerY, int(Camerabox.x) - width/2 - initWidth, height);
    rect(int(Camerabox.x) + initWidth/2 + 2, -centerY, (width-initWidth)/2, height);
    popMatrix();
    
    
      if(Player2.CurAction == Player2.Blocking || Player2.CurAction == Player2.BeingGrapped || Player2.CurAction == Player2.InHitStun || Player2.CurAction == Player2.Knockdown){
    Player2.draw();
    Player1.draw();
  }
  else if(Player1.CurAction == Player1.Blocking || Player1.CurAction == Player1.BeingGrapped || Player1.CurAction == Player1.InHitStun || Player1.CurAction == Player1.Knockdown){
         Player1.draw();
         Player2.draw();
  }
  else{
    Player1.draw();
    Player2.draw();
  }
  
    line(-initWidth, GROUNDHEIGHT, initWidth+initWidth/2, GROUNDHEIGHT);
  for(int i = VisEffectsList.size()-1; i >= 0; i--){
     VisEffectsList.get(i).draw();
  }
 
  StageBackground.drawForeground();
  
  Player1.drawBars(1);
  Player2.drawBars(-1);
        
  fill(0);
  textSize(20);
  //text("fps: "+frameRate, Camerabox.x, initHeight/2);
  showInputs(Player1.PlContr, Camerabox.x-initWidth/2, 200);
  showInputs(Player2.PlContr, Camerabox.x+initWidth/2-100, 200);
  
      }
    else{ frameFreeze--;}
   
  
  }
  
  void keyPressed(){  Player1.keyPressed();Player2.keyPressed();}
  void keyReleased(){  Player1.keyReleased();Player2.keyReleased();
    if(key == 'P' && !searchDevices)
      searchDevices = true;
    else if(key == 'P' && searchDevices) searchDevices = false;

  }
 
}

class F_Winscreen extends Gamestate{
  Gamestate savedGS;
  F_Winscreen(Gamestate savedGS){ this.savedGS = savedGS;}
  void gameLogic(){
         
    if(key == 'z' && keyPressed){
      resetPlayers();
      delCurGSlist();
     //CurGameStates.add( new F_2V2Fightmode() );
     CurGameState = new F_2V2Multiplayer();
     deletMe = true;
    }
    else
    if(key == 'h' && keyPressed){
      resetPlayers();
      delCurGSlist();
     //CurGameStates.add( new F_selectScreen(savedGS) );
     CurGameState = new F_selectScreen(new StageSelect(new F_2V2Multiplayer()));
     deletMe = true;
    }
   
  }
  
  void resetPlayers(){
        Player1.changeAction(Player1.Standing);
    Player2.changeAction(Player2.Standing);
    Player1.curHP = Player1.maxHP;
    Player2.curHP = Player2.maxHP;
    Player1.x = initWidth/4;
    Player2.x = initWidth-initWidth/4;
  }
  
  void drawVisuals(){
    savedGS.drawVisuals();      
         fill(255, 0,0);
         stroke(0);
         textSize(80);
         textMode(CENTER);
    if(Player1.curHP <= 0 && Player2.curHP <= 0){
      text("Gleichstand", Camerabox.x, Camerabox.y + initHeight/2);
    }     
    else if(Player2.curHP <= 0){
      text("Player1 Won", Camerabox.x, Camerabox.y + initHeight/2);
    }
    else if(Player1.curHP <= 0){
      text("Player2 Won", Camerabox.x, Camerabox.y + initHeight/2);
    }
    fill(0, 0,255); textSize(40);
    text("Press z for rematch", Camerabox.x, Camerabox.y + initHeight/2+80);
    text("Press h to return to select", Camerabox.x, Camerabox.y + initHeight/2+160);
}
  
}

class F_Editmode extends Gamestate{
  Fighter EditFighter; int eModeTimer = 0, eModeCurCBox = 0, eModeCurCAnim = 0, editItemId = 0, curFrame = 0, curCollumn = 0;
    EintragBox EditDiaBox = new EintragBox(20, 80, 100, 100, 5), EditDiaBox2 = new EintragBox(20, 180, 100, 100, 5), 
  EditFrameDurBox = new EintragBox(20, 280, 100, 100, 5),
  EditForceXBox = new EintragBox(width-width/10, 100, 100, 100, 5), EditForceYBox = new EintragBox(width-width/10, 200, 100, 100, 5),
  EditDatnamBox = new EintragBox(width-width/8, 0, 100, 100, 5);
  
  void gameLogic(){
    editMode(EditFighter, EditFighter.CurAction);
  }
  void drawVisuals(){}
  
  private int drawEditbar(int size, int curBoxState, int[] infBr, float y, float h){
      float l_xcord = 0; //int retCurBox = 0;
  for(int i = 0; i < size; i++){
        color f = color(255);
    if(i == curBoxState){
      f = color(255, 0,0);
    }
    
    float l_br = infBr[i] * initWidth / (1+sumOfArr(EditFighter.CurAction.whenToUpdBoxs) );
    MenuBox BoxFrame = new MenuBox(l_xcord, y, l_br, h, ""+ infBr[i], f);
        BoxFrame.draw();
    if(BoxFrame.clicked){
       return i;
    }
    
    l_xcord += l_br;
        
  }
    return curBoxState;
  }
  
  void editMode(Fighter EditPlayer, Action EditAction){
  background(120, 80, 80);
        EditPlayer.HitBoxes.clear();
        EditPlayer.HurtBoxes.clear();
  EditPlayer.x = width/2;
  EditPlayer.y = height/2+100;
  EditPlayer.CurAction = EditAction;
  EditPlayer.CurAction.curMoveDur = eModeTimer;
  EditPlayer.CurAction.curCollumn = eModeCurCBox;
  EditPlayer.CurAnimation = EditPlayer.CurAction.AttAnim;
  EditPlayer.CurAction.AttAnim.timer = eModeTimer;
  EditPlayer.CurAction.AttAnim.curCollumn = eModeCurCAnim;
  EditPlayer.draw();
  
  for(ColCircle c : EditPlayer.CurAction.HitBoxCollect.get(eModeCurCBox) ){
    fill(255, 0, 0, 70);
    c.setxy( EditPlayer.x, EditPlayer.y);
    c.draw(EditPlayer.dirMult);
  }
  for(ColCircle c : EditPlayer.CurAction.HurtBoxCollect.get(eModeCurCBox) ){
    fill(0, 0, 250, 70);
    c.setxy( EditPlayer.x, EditPlayer.y);
    c.draw(EditPlayer.dirMult);
  }
  
  //ArrayList<MenuBox> AnimFrames = new ArrayList<MenuBox>();
  //ArrayList<MenuBox> BoxFrames = new ArrayList<MenuBox>();
  
  eModeCurCAnim = drawEditbar(EditPlayer.CurAction.AttAnim.Sprites.length, eModeCurCAnim, EditPlayer.CurAction.AttAnim.changeTimes, initHeight-initHeight/10-40, 40);
  eModeCurCBox = drawEditbar(EditPlayer.CurAction.HitBoxCollect.size(), eModeCurCBox, EditPlayer.CurAction.whenToUpdBoxs, initHeight-initHeight/10, 40);
  
   switch(editItemId){
      case 2:
      textSize(6);
      float l_xcord = 0;
      Action pact = EditPlayer.CurAction;      
      for(int i = 0; i<pact.HitBoxCollect.size(); i++){
        float l_br = pact.whenToUpdBoxs[i] * initWidth / (1+sumOfArr(EditFighter.CurAction.whenToUpdBoxs) );
        MenuBox fB = new MenuBox(l_xcord, initHeight-initHeight/10-80, l_br, 40, pact.setForceAtDur[i][0] + "/" + pact.setForceAtDur[i][1], 255);
        fB.draw();
        if(fB.clicked){
          pact.updFrameDataArr_float(i, float(EditForceXBox.boxText), float(EditForceYBox.boxText));
        }
        l_xcord += l_br;
      }
      
      break;
    }
  
  MenuBox SetBox = new MenuBox(20, 20, 50, 50, "Set", 155);
  MenuBox ConfirmBox = new MenuBox(width-width/10, 300, 50, 50, "Confirm", 185);
  SetBox.draw();
  ConfirmBox.draw();
  EditDiaBox.draw();
  EditDiaBox2.draw();
  EditFrameDurBox.draw();
  EditForceXBox.draw(); 
  EditForceYBox.draw();
  EditDatnamBox.draw();

    if(click && SetBox.clicked){
      saveActionData(EditPlayer.CurAction, EditDatnamBox.boxText);
      println("saved");
    }
    else if(click && ConfirmBox.clicked){
      EditPlayer.CurAction.whenToUpdBoxs[eModeCurCBox] = int(EditFrameDurBox.boxText);
      println("changed");
    }

  else if(click && recPointCol(mouseX, mouseY, int(EditPlayer.x)-300, int(EditPlayer.y)-400, 600, 600)){ 
    
    if(mouseButton == LEFT){
      switch(editItemId){
        case 0:
    EditPlayer.CurAction.HitBoxCollect.get(eModeCurCBox).add(
    new ColCircle(mouseX-EditPlayer.x, mouseY-EditPlayer.y, int(EditDiaBox.boxText), int(EditDiaBox2.boxText), int(EditForceXBox.boxText), int(EditForceYBox.boxText), -1));
    break;
        case 1:
    EditPlayer.CurAction.HurtBoxCollect.get(eModeCurCBox).add(new ColCircle(mouseX-EditPlayer.x, mouseY-EditPlayer.y, int(EditDiaBox.boxText), int(EditDiaBox2.boxText) ));
    break;
        case 2:
    //EditPlayer.CurAction.updFrameDataArr_float(eModeCurCBox, mouseX-EditPlayer.x, mouseY-EditPlayer.y);
    break;
    
  }
      }
      
  if(mouseButton == RIGHT){
    
    if(editItemId == 0){
      for(int i = EditPlayer.CurAction.HitBoxCollect.get(eModeCurCBox).size()-1; i > -1; i--){
        ColCircle c = EditPlayer.CurAction.HitBoxCollect.get(eModeCurCBox).get(i);
        if(recPointCol(mouseX, mouseY, EditPlayer.x + c.addx - c.br/2, EditPlayer.y + c.addy - c.ho/2, c.br, c.ho) ){
          println("removed");
          EditPlayer.CurAction.HitBoxCollect.get(eModeCurCBox).remove(i);

        }
      }
    }
    else if(editItemId == 1){
      for(int i = EditPlayer.CurAction.HurtBoxCollect.get(eModeCurCBox).size()-1; i > -1; i--){
        ColCircle c = EditPlayer.CurAction.HurtBoxCollect.get(eModeCurCBox).get(i);
        if(recPointCol(mouseX, mouseY, EditPlayer.x + c.addx - c.br/2, EditPlayer.y + c.addy - c.ho/2, c.br, c.ho) ){
          EditPlayer.CurAction.HurtBoxCollect.get(eModeCurCBox).remove(i);
        }
      }
    }
    
  }
  
  }
 
  textSize(10);
  text("CurCollumm: " + eModeCurCBox + " |CurFrame: " +EditPlayer.CurAction.sumOfFrameArr(eModeCurCBox) + " |EditItemId: " + editItemId, 100, 50);

}
  
  void keyReleased(){
     EditDiaBox.keyReleased();
  EditDiaBox2.keyReleased();
  EditFrameDurBox.keyReleased();
  EditForceXBox.keyReleased(); 
  EditForceYBox.keyReleased();
  EditDatnamBox.keyReleased();
  
    camera();
  
    Action EAct = EditFighter.CurAction;
    
    if(keyCode == LEFT  && 1 < EAct.HitBoxCollect.size()){
    EAct.HitBoxCollect.remove(EAct.HitBoxCollect.size()-1);
    EAct.HurtBoxCollect.remove(EAct.HurtBoxCollect.size()-1);
    EAct.whenToUpdBoxs = int_copyArrToSmallSize(EAct.whenToUpdBoxs, EAct.HitBoxCollect.size());
    EAct.setForceAtDur = float_copyArrToSmallSize(EAct.setForceAtDur, EAct.HitBoxCollect.size());
  }
  else if(keyCode == LEFT  && 1 >= EAct.HitBoxCollect.size()){
    EAct.whenToUpdBoxs[0] = int(EditFrameDurBox.boxText);
    EAct.whenToUpdBoxs = int_copyArrToSmallSize(EAct.whenToUpdBoxs, EAct.HitBoxCollect.size());
    EAct.setForceAtDur = float_copyArrToSmallSize(EAct.setForceAtDur, EAct.HitBoxCollect.size());
    // TO DO: add same for force Array
  }
  else if(keyCode == RIGHT && EAct.HitBoxCollect.size() == EAct.whenToUpdBoxs.length){

    EAct.addAllLists( EAct.HitBoxCollect.size()-1 , int(EditFrameDurBox.boxText), 0, 0);
    println("addedList");
  }
  else if(keyCode == RIGHT && EAct.HitBoxCollect.size() < EAct.whenToUpdBoxs.length){
         EAct.addAllLists( EAct.HitBoxCollect.size()-1 , int(EditFrameDurBox.boxText), 0, 0);
  }

    if(keyCode == DOWN && editItemId > 0){
    editItemId--;
  }
  else if(keyCode == UP){
    editItemId++;
  } 

  }
  
  void finishState(){ eModeTimer = 0; eModeCurCBox = 0; eModeCurCAnim = 0; }
  
}

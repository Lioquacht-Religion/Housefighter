class MenuElements{
}

class Training_Menu extends MenuElements{
  int mState = 0;
  MenuBox Movelistbox = new MenuBox(initWidth*0.75, 50, 50, 200, "MOVELIST", 255), StateBox = new MenuBox(initWidth*0.75, 100, 50, 200, "State", 255),
  recordBox = new MenuBox(initWidth*0.75, 150, 50, 200, "Record", 255), BackToMain = new MenuBox(initWidth*0.75, 200, 50, 200, "MAINMENU", 255), 
  Backbox = new MenuBox(initWidth-initWidth/8, 450, 100, 50, "Back", 255);
  
  FlickBox AIbox = new FlickBox(0, 50, 50, 200, "AI"), Blockbox = new FlickBox(0, 100, 50, 200, "Blocking"), easyPlayBox = new FlickBox(0, 150, 50, 200, "easyPlay"),
  OppTechBox = new FlickBox(0, 200, 50, 200, "AutoTech");
  MenuBox[] MBlist;
  MenuBox[][] MBGrid;
  GridCursor<MenuBox> GCursor;
  
  Training_Menu(){
    MBlist = new MenuBox[]{Movelistbox, AIbox, Blockbox, StateBox, recordBox, BackToMain, easyPlayBox, OppTechBox}; 
    MBGrid = new MenuBox[][]{ 
  {AIbox, Movelistbox},
  {Blockbox, StateBox},
  {easyPlayBox, recordBox},
  {OppTechBox, BackToMain},
  {Backbox, Backbox}};
  
  GCursor = new GridCursor<MenuBox>(MBGrid, 0, 0, Con1, 4, 6);
  }
  
  void draw(Fighter P1, Fighter P2){
    switch(mState){
      case 0: 
      
        showInputs(Con1, 100, 250);
    Con1.deviceInput(); GCursor.Logic(); 
    Con1.draw();  GCursor.draw(0, 0);
    
    if(Movelistbox.clicked) mState = 1;
    if(BackToMain.clicked) CurGameState = new MainMenu();
    
      InputRecord r = Player2.Recorder; InputRecord r2 = Player2.Recorder;
  if(recordBox.clicked){
    
    switch(r.state){
      case 0: r.state = 1; r2.state = 1; println("recording"); break;
      case 1: r.state = 2; r2.state = 2; println("play"); r.reset(); r2.reset(); break;
      case 2: r.state = 0; r2.state = 0; println("nothing"); r.printRec(); r.reset(); r2.printRec(); r2.reset();break;
      default: r.state = 0; r2.state = 0; break;
    }
  
  }
  
      for(MenuBox MB : MBlist){
      
      MB.breite = 200; MB.hoehe = 50;
      pushMatrix();
      translate(Camerabox.x-Camerabox.br/2, Camerabox.y);
      MB.draw();
      popMatrix();
    }
    break;
    
    case 1:
    moveList(P1, 40);
    moveList(P2, initWidth/2);
    break;
  
  }
        pushMatrix(); translate(Camerabox.x-Camerabox.br/2, Camerabox.y);
  Backbox.draw(); if(Backbox.clicked) mState = 0;  popMatrix();
  
}

void inGameEffect(Fighter P1, Fighter P2){
  if(easyPlayBox.flicked){P1.easyMode = true; P2.easyMode = true;} else {P1.easyMode = false; P2.easyMode = false;};
  if(AIbox.flicked){P2.AI_Controlled = true;} else {P2.AI_Controlled = false;};
  if(OppTechBox.flicked && P2.CurAction == P2.softKD){P2.firstPressInp[4] = true; P2.inputs[4] = true;}

}

String intArrToString(int[] arr){
  String s = "";
  for(int i = 0; i < arr.length; i++){ s += arr[i]; }
  return s;
}

void moveList(Fighter F, float x){
  noStroke(); fill(0, 200, 40);
  rect(Camerabox.x-Camerabox.br/2+x, Camerabox.y, 300, initHeight);
    fill(0); textSize(14); textAlign(CORNER);
  int l_hMult = 0;
  for(int i = 0; i < F.ActionList.size(); i++){
    for(int j = 0; j < F.ActionList.get(i).size(); j++){
      Action a = F.ActionList.get(i).get(j);
      String inputReq = "";
      for(Condition c : a.Conds){
        
        if(c instanceof comfPButC){
          inputReq += intArrToString( ((comfPButC) c).motions[0] );
        }
        else if(c instanceof dirCombCond){
          inputReq += intArrToString( ((dirCombCond) c).motion );
        }
        
      }
      for(Condition c : a.Conds){
        if(c instanceof fPButCond){
          inputReq += " + " + ((fPButCond) c).ButIndex;
        }
      }
      
      if( !(inputReq.equals("")) ){text(a.datnam + ": "+ inputReq, Camerabox.x-Camerabox.br/2+x, Camerabox.y+l_hMult*20+40); l_hMult++;}
      
    }
  }
}


}

class MP_Menu extends Training_Menu{
  MP_Menu(){
    MBlist = new MenuBox[]{Movelistbox, AIbox, BackToMain, easyPlayBox};
    MBGrid = new MenuBox[][]{ 
  {AIbox, Movelistbox},
  {easyPlayBox, easyPlayBox},
  {BackToMain, BackToMain},
  {Backbox, Backbox}};
  
  GCursor = new GridCursor<MenuBox>(MBGrid, 0, 0, Con1, 4, 6);
  }
}


class MenuBox extends MenuElements{
  float x;
  float y;
  float breite;
  float hoehe;
  String boxText = "";
  color farbe = 255;
  boolean clicked = false;
  
public MenuBox(float xmb, float ymb, float bmb, float hmb){
   this.x = xmb;
   this.y = ymb;
   this.breite = bmb;
   this.hoehe = hmb;
}
  
public MenuBox(float xmb, float ymb, float bmb, float hmb, String boxTxtmb, color f){
  this(xmb, ymb, bmb, hmb);
   this.boxText = boxTxtmb;
   this.farbe = f;
 }

void draw(){
  menuBoxClick();
  displayMenuBox();
}
 
  
void displayMenuBox(){
  rectMode(CORNER);
  stroke(0);
  fill(this.farbe);
  rect(x, y, breite, hoehe);
  textSize(40);
  textAlign(CENTER);
  fill(0);
  text(this.boxText,x+breite/2,y+hoehe/2);  
  
  if(mouseInBox()){
    blink(x + breite, y +hoehe/2);
  }
} 

void menuBoxClick(){
  if(click && mouseInBox()){
    clicked = true;
  }else clicked = false;
}

boolean mouseInBox(){
    float xScale = float(width) / float(initWidth), yScale = float(height) / float(initHeight);
  return x * xScale <= mouseX && mouseX <= (x + breite)*xScale && y * yScale <=mouseY && mouseY <= (y + hoehe)*yScale;
}

void blink(float xb, float yb){
  float xC, yC;
 
    yC = yb;
    xC = xb - (millis() % 10);
    
     triangle(xC,yC,xC+20,yC-10,xC+20,yC+10);
}

void umrandung(){
    rectMode(CORNER);
    fill(0);
    rect(x-breite/10,y-hoehe/10, breite +breite/10*2, hoehe +hoehe/10*2); 
}

}

class CharSelectKachel extends MenuBox{
  PImage BG, BigCharArt; Animation CharSprs;
  
  CharSelectKachel(float xmb, float ymb, float bmb, float hmb, PImage BG, Animation CharSprs, PImage BigCharArt){
    super(xmb, ymb, bmb, hmb);
    this.BG = BG; this.CharSprs = CharSprs; this.BigCharArt = BigCharArt;
  }
  
  void displayMenuBox(){
    rectMode(CORNER);
    imageMode(CORNER);
    image(BG, x, y);
    CharSprs.draw(x, y);
    CharSprs.handleAnim();
  }
  
}

class FlickBox extends MenuBox{
  boolean flicked = false;
  FlickBox(float xmb, float ymb, float bmb, float hmb, String boxTxtmb){
        super(xmb, ymb, bmb, hmb, boxTxtmb, 255);
  }
  
  void draw(){
  menuBoxClick();
  if(clicked && !flicked){flicked = true; farbe = color(255, 0, 0);} else if(clicked && flicked){flicked = false; farbe = 255;}
  displayMenuBox();
}

}


class EintragBox extends MenuBox{
  int count = 0;
  int maxCount;
  
  EintragBox(float xmb, float ymb, float bmb, float hmb, int maxCount){
    super(xmb, ymb, bmb, hmb);
    this.maxCount = maxCount;
  }
  
  EintragBox(float xmb, float ymb, float bmb, float hmb){
    this(xmb, ymb, bmb, hmb, 30);
  }
  EintragBox(float xmb, float ymb, float bmb, float hmb, int maxCount, String boxText){
    this(xmb, ymb, bmb, hmb, maxCount); this.boxText = boxText; count = boxText.length();
  }
  
  void draw(){
    eintragClick();
    displayMenuBox();
  }
  
  void eintragClick(){
    if(mousePressed && mouseX > x && mouseX < x + breite && mouseY > y && mouseY < y+hoehe){
    clicked = true;
  }else if(!(mouseX > x && mouseX < x + breite && mouseY > y && mouseY < y+hoehe)){
    clicked = false;
  }
  if(clicked){
    umrandung();
  }
  }
  
  void keyReleased(){
  if(clicked){
  if(count >= maxCount){
    //boxText = "";
    //count = 0;
  }
  
    if(keyCode == 8 //&& count > 0
    ){
   String Wort = "";
   for(int i = 0; i < boxText.length()-1; i++){
     Wort += boxText.charAt(i);
   }
   boxText = Wort;
   //count--;
   //Eingabe = Eingabe.replaceFirst(Eingabe.substring(Eingabe.length()-1), "");
 }  else {//if((keyCode >= 48 && keyCode <= 57) || key == '-'){
 
    boxText = boxText + key;
    //count++;
 }
}
}
  
}

/*
  2D Fighter Prototype
  
CONTROLLS:
- Player1                  ; Player2
- Richtungen: w, a, s, d   ; o, k, l, ö
- Angriffe: e, r, f        ; i, u, j
- Universal technik: x     ; m
- c: Hitboxeditor öffnen
- v: Hitboxeditor schließen
- M: Programm schließen
- B: KI Spieler2 einschalten
- G: SP1 Charakter wechseln
- H: SP2 Charakter wechseln

TO DO:
- Ducken
- Blocken (Oben, Unten Blocken)
- Hitstun
- Kollisionsboxen
- Umwerfen/Liegen
- Wakeup
- Griffe/würfe
- Projektile
- Kombinationseingaben/ Schlagangriffe
- unbedingt moltschat doma referenzen einbauen
- controller support
- online support
- Gegner KI
- Stagegrenzen und Bewegung, sowie Kamerabewegung

- light/hardknockdown 
- Air reset/ recovery
- juggle state
- wall/ground bounce and wall stick
- stagger

- specialcancels

BUGS:
- Actions können in ihrem collumn stecken bleiben, wenn vorher gecancelt durch anderen Angriff -> Funktion Action.reset() nutzen
- HitStun wird mit jeden hit erhöht -> nicht mehr so, eventuell immer noch buggy
- es ist kein richtiges abrollen füt FB o. DB möglich, wegen der diagonalen -> seltsames doppelspringen
- Blocken geht nicht immer -> scheint gefixt zu sein

*/

import processing.net.*;
Server s1, s2;
Client c1, c2;
String othersIp;


import net.java.games.input.*;
import org.gamecontrolplus.*;
import org.gamecontrolplus.gui.*;

import ddf.minim.*;
Minim minim;
AudioPlayer[] Soundeffects = new AudioPlayer[5];

ControlIO control;

ControlDevice device;
ControlDevice device2;

EintragBox EditDiaBox = new EintragBox(20, 80, 100, 100, 5);
EintragBox EditDiaBox2 = new EintragBox(20, 180, 100, 100, 5);
EintragBox EditFrameDurBox = new EintragBox(20, 280, 100, 100, 5);
EintragBox EditForceXBox;
EintragBox EditForceYBox;
EintragBox EditDatnamBox;

Stage StageBackground; PImage SBextra;

int frame = 0;

final int GROUNDHEIGHT = 460;  // war 620
int initWidth, initHeight;
boolean click = false;
boolean editMode = false;
boolean playAction = false;
int editItemId = 0, curFrame = 0, curCollumn = 0;

PImage[] Proj_sprs = new PImage[2];

ArrayList<VisualEffect> VisEffectsList= new ArrayList<VisualEffect>();

char[] inputCharPl1 = {'w', 's', 'd', 'a', 'e', 'f', 'r', 'x'};
char[] inputCharPl2 = {'o', 'l', 'ö', 'k', 'i', 'j', 'u', 'm'};

Fighter Player1;
Fighter Player2;

Animation HitEff;
Animation BlockEff;
Animation dustJumpEff, RCEff, BurstEff;

int stageWidth = 0;
  int frameFreeze = 0;
  int slowMoDur, slowMoValue;
  ColRect Camerabox;
  int gameState = 0;
  int P1Wins = 0, P2Wins = 0;

void setup(){

  initWidth = 900;
  initHeight = 500;
  stageWidth = initWidth*2;
  StageBackground = new Stage("BergGroßStage/0Stage", 6, 5);
  SBextra = loadImage("FHouse_AirMid.png");
  for(int i = 0; i < Proj_sprs.length; i++){
    Proj_sprs[i] = loadImage("Proj_spr" + i + ".png"); 
  }
  minim = new Minim(this);
  for(int i = 0; i < Soundeffects.length; i++){
    Soundeffects[i] = minim.loadFile("Soundeffekte/sound" + i + ".wav");
  }
  
  int[] times0 = {2, 2, 3, 3, 3, 3, 2, 2}; int x0 = 0;
  HitEff = new Animation(times0, x0, 8, "Effekte/HitEff/HitEffekt");
  int[] times1 = {2, 2, 2, 2, 2, 2, 2, 2, 2}; int x1 = 0;
  BlockEff = new Animation(times1, x1, 9, "Effekte/BlockEff/BlockEffekt");
  int[] times3 = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
  dustJumpEff = new Animation(times3, 0, 14, "Effekte/dustJumpEff/n.dustJumpEff");
    int[] times4 = {2, 2, 2, 2, 2, 2};
  RCEff = new Animation(times4, 0, 6, "Effekte/RCEff/RCEff");
    int[] times5 = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
  BurstEff = new Animation(times5, 0, 10, "Effekte/BurstEff/BurstEff");
  
  control = ControlIO.getInstance(this);
  device = control.getMatchedDevice("HFcontroller");
  println("p1Connected");
  //device2 = control.getMatchedDevice("HousefighterGamepadCon");
  println("p2Connected");
  if(device == null){
    println("No, just no");
    //System.exit(-1);
  }
    
  s1 = new Server(this, 10000);//new Server(this, 5204);
  othersIp = "2003:c2:470b:b260:9dfa:8f3a:3dd9:c598";
  
  Player1 = new F_WHaus(initWidth/4, GROUNDHEIGHT, inputCharPl1, device);
  //false, false);
  Player2 = new F_HHaus(initWidth-initWidth/4, GROUNDHEIGHT, inputCharPl2, //device2);
  true, false);
  Player1.setup();
  Player2.setup();
  
  
  Camerabox = new ColRect(initWidth/2.0, 0, initWidth, initHeight);
  
    frameRate(60);
  size(900, 500, P3D);
  //fullScreen(P3D);
  
    EditForceXBox = new EintragBox(width-width/10, 100, 100, 100, 5);
  EditForceYBox = new EintragBox(width-width/10, 200, 100, 100, 5);
  EditDatnamBox = new EintragBox(width-width/8, 0, 100, 100, 5);
  
}
  
void netcode(){
  
  String data = "";
    for(int j = 0; j < Player1.inputs.length; j++){
      data += "" + Player1.inputs[j] + " ";
    }
          s1.write(data + "\n");
          
      //while(c1.available() <= 0){
      //}
  
       if (c1.available() > 0) { 
     String input = "" + c1.readString(); 
     println(c1.readString() );
     println("P1" + input + input.indexOf('\n'));
    input = input.substring(0, input.indexOf('\n'));  // Only up to the newline
    Player2.inputs = boolean(split(input, ' '));  // Split values into an array
   
  }
    //c1.clear();
   
}
  
void draw(){
  
  switch(gameState){  
    case 0:
    selectScreen();
    break;
    
    case 1:
    if(!editMode){
    
  for(int i = 0; i < round(60/frameRate) && frameRate >= 29 && frameFreeze <= 0; i++){
    /*if(send){
          netcode();
    }*/

    //if(c1 != null){
      //if(c1.available() >= 0){
        if(slowMoDur % (slowMoValue+1) == 0){
          battleMode();
        }
      //}
   // }
      if(slowMoDur > 0){
          slowMoDur--;
      }
       if(frame >= 60){
         frame = 0;
        }
        else{ frame++; }
  }
    
  battleModeVisuals();
  }
  else{ 
     editMode(Player1, Player1.CurAction);
  }
  
  if(Player1.curHP <= 0 || Player2.curHP <= 0){
        if(Player1.curHP <= 0 && Player2.curHP <= 0){
    }     
    else if(Player2.curHP <= 0){
      P2Wins++;
    }
    else if(Player1.curHP <= 0){
      P1Wins++;
    }
    gameState = 2;
  }
  
  if(key == 'M'){
    //exit();
  }
  
  break;
  
  case 2:
     battleModeVisuals();
     winScreen();
  break;
  
  }
  click = false;
  
}

void selectScreen(){
      camera(initWidth/2, initHeight/2.0, (initHeight/2.0) / tan(PI*30.0 / 180.0),
    initWidth/2, initHeight/2.0, 0,
    0, 1, 0);
  background(180, 0, 255);
  MenuBox[] charBoxs = new MenuBox[3];
  MenuBox BackBox = new MenuBox(initWidth/2, initHeight/2+100, 80, 50, "BACK", color(255, 0, 0) );
  MenuBox StartBox = new MenuBox(initWidth/2, initHeight/2+160, 100, 70, "START", color(0, 255, 0) );
  StartBox.draw();
  BackBox.draw();
  for(int i = 0; i < charBoxs.length; i++){
    charBoxs[i] = new MenuBox(initWidth/4+ 100 + 40*i, initHeight/2, 40, 40, "Char"+i, color(255) );
    charBoxs[i].draw();
    if(charBoxs[i].clicked){
      if(Player1 == null){
        Player1 = chooseFighter( i, initWidth/4, GROUNDHEIGHT, inputCharPl1);
       
      }
      else if(Player2 == null){
        Player2 = chooseFighter( i, initWidth-initWidth/4, GROUNDHEIGHT, inputCharPl2);
        
      }
    }
  }
  if(BackBox.clicked){
      if(Player2 != null){
        Player2 = null;
      }
      else if(Player1 != null){
        Player1 = null;
      }
  }
  imageMode(CENTER);
 if(Player1 != null){Player1.CurAction = Player1.Standing; Player1.CurAction.AttAnim.handleAnim(); image(Player1.CurAction.AttAnim.Sprites[Player1.CurAction.AttAnim.curCollumn],
 initWidth/4, initHeight/2);}
 if(Player2 != null){Player2.CurAction = Player2.Standing; Player2.CurAction.AttAnim.handleAnim(); image(Player2.CurAction.AttAnim.Sprites[Player2.CurAction.AttAnim.curCollumn], 
 initWidth-initWidth/4, initHeight/2);}
  
  if(StartBox.clicked && Player1 != null && Player2 != null){
    gameState = 1;
    Player1.setup();
    if(device != null){
    Player1.device = device;
    Player1.setConDevice();
    }
    Player2.setup();
    if(device2 != null){
    Player2.device = device2;
    Player2.setConDevice();
    }
  }

}

Fighter chooseFighter(int index, int x, int y, char[] charinputs){
  //Fighter F = new F_FHaus(x, y, charinputs);
  if(index == 0){
    return new F_FHaus(x, y, charinputs);
  }else if(index == 1){
    return new F_HHaus(x, y, charinputs);
  }else if(index == 2){
    return new F_WHaus(x, y, charinputs);
  }
  return new F_FHaus(x, y, charinputs);
}
  
void winScreen(){
         if(Player1.curHP <= 0 || Player2.curHP <= 0){
    
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
         
    if(key == 'z' && keyPressed){
    Player1.changeAction(Player1.Standing);
    Player2.changeAction(Player2.Standing);
    Player1.curHP = Player1.maxHP;
    Player2.curHP = Player2.maxHP;
    Player1.x = initWidth/4;
    Player2.x = initWidth-initWidth/4;
    gameState = 1;
    }
    if(key == 'h' && keyPressed){
      gameState = 0;
    }
    
  }
}

void stop(){
  minim.stop();
}


int eModeTimer = 0;
int eModeCurCBox = 0;
int eModeCurCAnim = 0;

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
  
  ArrayList<MenuBox> AnimFrames = new ArrayList<MenuBox>();
  ArrayList<MenuBox> BoxFrames = new ArrayList<MenuBox>();
  

  int l_xcord = 0;
  for(int i = 0; i < EditPlayer.CurAction.AttAnim.Sprites.length; i++){
    color f = color(255);
    if(i == eModeCurCAnim){
      f = color(255, 0,0);
    }
    
    float l_br = EditPlayer.CurAction.AttAnim.changeTimes[i] * width /  sumOfArr(EditPlayer.CurAction.whenToUpdBoxs);//sumOfArr(EditPlayer.CurAction.AttAnim.changeTimes);
    AnimFrames.add(new MenuBox(l_xcord, height-height/6, l_br, 40, ""+ EditPlayer.CurAction.AttAnim.changeTimes[i], color(f)) );
        AnimFrames.get(i).draw();
    if(AnimFrames.get(i).clicked){
      eModeCurCAnim = i;
    }
    
    l_xcord += l_br;
  }
//#######################  
  l_xcord = 0;
  for(int i = 0; i < EditPlayer.CurAction.HitBoxCollect.size(); i++){
        color f = color(255);
    if(i == eModeCurCBox){
      f = color(255, 0,0);
    }
    
    float l_br = EditPlayer.CurAction.whenToUpdBoxs[i] * width / sumOfArr(EditPlayer.CurAction.whenToUpdBoxs);
    BoxFrames.add(new MenuBox(l_xcord, height-height/10, l_br, 40, ""+ EditPlayer.CurAction.whenToUpdBoxs[i], f) );
        BoxFrames.get(i).draw();
    if(BoxFrames.get(i).clicked){
      eModeCurCBox = i;
    }
    
    l_xcord += l_br;
  }
  
  
  MenuBox SetBox = new MenuBox(20, 20, 50, 50, "Set", 155);
  MenuBox ConfirmBox = new MenuBox(width-width/10, 300, 50, 50, "Confirm", 185);
  //MenuBox StartBox = new MenuBox(450, 20, 40, 30, "Start", 255);
  SetBox.draw();
  ConfirmBox.draw();
  EditDiaBox.draw();
  EditDiaBox2.draw();
  EditFrameDurBox.draw();
  EditForceXBox.draw(); 
  EditForceYBox.draw();
  EditDatnamBox.draw();
  //StartBox.draw();

 

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
  if(editItemId == 0){
    EditPlayer.CurAction.HitBoxCollect.get(eModeCurCBox).add(new ColCircle(mouseX-EditPlayer.x, mouseY-EditPlayer.y, int(EditDiaBox.boxText), int(EditDiaBox2.boxText), int(EditForceXBox.boxText), int(EditForceYBox.boxText), -1));
    
  }
  else if(editItemId == 1){
    EditPlayer.CurAction.HurtBoxCollect.get(eModeCurCBox).add(new ColCircle(mouseX-EditPlayer.x, mouseY-EditPlayer.y, int(EditDiaBox.boxText), int(EditDiaBox2.boxText) ));
   
  }
  else if(editItemId == 2){
    EditPlayer.CurAction.updFrameDataArr_float(eModeCurCBox, mouseX-EditPlayer.x, mouseY-EditPlayer.y);
    
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

void editModeKeyRe(Fighter EditPlayer){
  camera();
  
  if(key == 'c' && !editMode){
    editMode = true;
        EditPlayer.HitBoxes.clear();
        EditPlayer.HurtBoxes.clear();
        eModeTimer = 0;
        eModeCurCBox = 0;
        eModeCurCAnim = 0;
        EditDatnamBox.boxText = EditPlayer.CurAction.datnam;
  }
  else 
  if(key == 'v' && editMode){
    editMode = false;
        eModeTimer = 0;
        eModeCurCBox = 0;
        eModeCurCAnim = 0;
  }
  
  if(key == 'B' && !Player2.AI_Controlled){
    Player2.AI_Controlled = true;
  }
  else  if(key == 'B' && Player2.AI_Controlled){
    Player2.AI_Controlled = false;
  }
  
  if(key == 'G'){
    if(Player1 instanceof F_FHaus){
      Player1 = new F_HHaus(initWidth/4, GROUNDHEIGHT, inputCharPl1, false, false);
    }
    else if(Player1 instanceof F_HHaus){
      Player1 = new F_FHaus(initWidth/4, GROUNDHEIGHT, inputCharPl1, false, false);
    }
    Player1.setup();
  }
  
    if(key == 'H'){
    if(Player2 instanceof F_FHaus){
      Player2 = new F_HHaus(initWidth-initWidth/4, GROUNDHEIGHT, inputCharPl2, true, false);
    }
    else if(Player2 instanceof F_HHaus){
      Player2 = new F_FHaus(initWidth-initWidth/4, GROUNDHEIGHT, inputCharPl2, true, false);
    }
    Player2.setup();
  }
  
  if(editMode){
    
    if(keyCode == LEFT  && 1 < EditPlayer.CurAction.HitBoxCollect.size() && editMode){
    EditPlayer.CurAction.HitBoxCollect.remove(EditPlayer.CurAction.HitBoxCollect.size()-1);
    EditPlayer.CurAction.HurtBoxCollect.remove(EditPlayer.CurAction.HurtBoxCollect.size()-1);
    EditPlayer.CurAction.whenToUpdBoxs = int_copyArrToSmallSize(EditPlayer.CurAction.whenToUpdBoxs, EditPlayer.CurAction.HitBoxCollect.size());
    EditPlayer.CurAction.setForceAtDur = float_copyArrToSmallSize(EditPlayer.CurAction.setForceAtDur, EditPlayer.CurAction.HitBoxCollect.size());
  }
  else if(keyCode == LEFT  && 1 >= EditPlayer.CurAction.HitBoxCollect.size() && editMode){
    EditPlayer.CurAction.whenToUpdBoxs[0] = int(EditFrameDurBox.boxText);
    EditPlayer.CurAction.whenToUpdBoxs = int_copyArrToSmallSize(EditPlayer.CurAction.whenToUpdBoxs, EditPlayer.CurAction.HitBoxCollect.size());
    EditPlayer.CurAction.setForceAtDur = float_copyArrToSmallSize(EditPlayer.CurAction.setForceAtDur, EditPlayer.CurAction.HitBoxCollect.size());
    // TO DO: add same for force Array
  }
  else if(keyCode == RIGHT && editMode && EditPlayer.CurAction.HitBoxCollect.size() == EditPlayer.CurAction.whenToUpdBoxs.length){

    EditPlayer.CurAction.addAllLists( EditPlayer.CurAction.HitBoxCollect.size()-1 , int(EditFrameDurBox.boxText), 0, 0);
    println("addedList");
  }
  else if(keyCode == RIGHT && editMode && EditPlayer.CurAction.HitBoxCollect.size() < EditPlayer.CurAction.whenToUpdBoxs.length){
         EditPlayer.CurAction.addAllLists( EditPlayer.CurAction.HitBoxCollect.size()-1 , int(EditFrameDurBox.boxText), 0, 0);
  }

    if(keyCode == DOWN && editItemId > 0 && editMode){
    editItemId--;
  }
  else if(keyCode == UP && editMode){
    editItemId++;
  } 
  
  }
}

boolean send = false;
void keyPressed(){
  Player1.keyPressed();
  Player2.keyPressed();
}

void keyReleased(){
  Player1.keyReleased();
  Player2.keyReleased();
  
  EditDiaBox.keyReleased();
  EditDiaBox2.keyReleased();
  EditFrameDurBox.keyReleased();
  EditForceXBox.keyReleased(); 
  EditForceYBox.keyReleased();
  EditDatnamBox.keyReleased();
  
    editModeKeyRe(Player1);
    
    if(key == 'Z' && !send){
      send = true;
      c1 = new Client(this, othersIp, 12346);
    }
  
}


void mouseClicked(){
  click = true;
}

void battleMode(){
  
   Player1.gameLogic(Player2);
   Player2.gameLogic(Player1);
   
    ColCircle[] l_Boxes2 = Player2.checkHit(Player1);
    ColCircle[] l_Boxes1 = Player1.checkHit(Player2);
    
    Action l_P1Act = Player1.operationsOnHit(Player2, l_Boxes1);
    Action l_P2Act = Player2.operationsOnHit(Player1, l_Boxes2);
    if(l_Boxes1 != null){
    Player2.CurAction.reset();
    Player2.CurAction = l_P1Act;
    }
    if(l_Boxes2 != null){
    Player1.CurAction.reset();
    Player1.CurAction = l_P2Act;
    }
    
  Player1.facingCheckAndChange(Player2);
  Player2.facingCheckAndChange(Player1);
  
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
          
  
  line(-initWidth, GROUNDHEIGHT, initWidth+initWidth/2, GROUNDHEIGHT);
  for(int i = VisEffectsList.size()-1; i >= 0; i--){
    if(VisEffectsList.get(i).exTimer <= 0){
      VisEffectsList.remove(i);
    }
  }
  
}


void battleModeVisuals(){
  
    if(frameFreeze <= 0){
  //centerX = initWidth/2.0;//dist(Player1.x, 0, Player2.x, 0)/2;
  float centerZ = 0;
  float centerY = 0;
  float centerZ2 = centerZ / tan(PI*30.0 / 180.0);
  
  if(Player1.x < Player2.x){
    Camerabox.x = Player1.x + dist(Player1.x, 0, Player2.x, 0)/2;
  }
  else {
    Camerabox.x = Player1.x - dist(Player1.x, 0, Player2.x, 0)/2;
  }
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
  println("start drawing background");
    StageBackground.drawBackground();
    println("finished");
    //image(SBextra, 0, -200);
    //auslagern in fighter spezifische funktionen
    drawBar( int(Camerabox.x) - initWidth/4, 20 - int(centerY) , centerZ2, Player1.maxHP, Player1.curHP, -180);
    drawBar( int(Camerabox.x) + initWidth/4, 20 - int(centerY), centerZ2, Player2.maxHP, Player2.curHP, 180);
    drawBar( int(Camerabox.x) - initWidth/4, GROUNDHEIGHT+10 - int(centerY) , centerZ2, Player1.maxSuper, Player1.curSuper, -140);
    drawBar( int(Camerabox.x) + initWidth/4, GROUNDHEIGHT+10 - int(centerY), centerZ2, Player2.maxSuper, Player2.curSuper, 140);
    showInputs(Player1, Camerabox.x, 200);
    
    fill(0);
    textSize(30);
    
    if(Player1.comboCount > 0){
      text("COMBO:" + Player1.comboCount, int(Camerabox.x) - initWidth/2, 200 - int(centerY));
    }
    if( Player2.comboCount > 0){
      text("COMBO:" + Player2.comboCount, int(Camerabox.x) + initWidth/4, 200 - int(centerY));
    }
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
   println("start drawing foreground");
  StageBackground.drawForeground();
   println("finished");
        
  fill(0);
  textSize(20);
  text("fps: "+frameRate, Camerabox.x, initHeight/2);
  
      }
    else{ frameFreeze--;}
}

void drawBar(int x, int y, float z, float maxBar, float curBar, float scale){
  float curBarLength = curBar / maxBar * scale;
  pushMatrix();
  translate(0, 0, z);
  fill(20, 70, 20);
  rect(x, y, scale, 20);
    fill(200, 30, 20);
  rect(x, y, curBarLength, 20);
  popMatrix();
}

void showInputbuffer(Fighter Pl, int x, int y){
      fill(0);
  for(int i = 0; i < Pl.inputbufferDir.size(); i++){

    text(Pl.inputbufferDir.get(i), x, y + 20*i);
  }
  for(int i = 0 ; i < Pl.inputbufferBut.size(); i++){
    for(int j = 0; j < Pl.inputbufferBut.get(i).size(); j++){
      String output = "";
      if(Pl.inputbufferBut.get(i).get(j) == 10){
        output += "R";
      }
      else if(Pl.inputbufferBut.get(i).get(j) == 11){
        output += "M";
      }
      else if(Pl.inputbufferBut.get(i).get(j) == 12){
        output += "B";
      }
       text(output, x + 30 * (j+1), y + 20*i);
    }
  }
  
}

void showInputs(Fighter F, float x, float y){
  pushMatrix();
  translate(x, y);
  fill(0, 30);
  ellipse(0, 0, 60, 60);
  for(int i = 0; i < 4; i++){
    if(F.inputs[i]){
      fill(200, 0, 0);
      rect(21*i, 0, 20, 20);
    }
  }  
    for(int i = 4; i < 7; i++){
    if(F.inputs[i]){
      fill(60*i, 0, 200);
      rect(21*(i-3), -30, 20, 20);
    }
  }
  
  popMatrix();
}

  
boolean[] inputsKeyCode(boolean setBoolsTo, int[] keyCodes, boolean[] boolsToSet){
  for(int i = 0; i < keyCodes.length; i++){
      if(keyCode == keyCodes[i]){
    boolsToSet[i] = setBoolsTo;
  }
  }
  return boolsToSet;
}

boolean[] inputsKey(boolean setBoolsTo, char[] keys, boolean[] boolsToSet){
  for(int i = 0; i < keys.length; i++){
      if(key == keys[i]){
    boolsToSet[i] = setBoolsTo;
  }
  }
  return boolsToSet;
}

void saveActionData(Action a, String datnam){
  JSONObject data = new JSONObject();
  data.setInt("BoxsSize", a.HitBoxCollect.size());
  for(int i = 0; i < a.HitBoxCollect.size(); i++){
        data.setInt("whenUpd"+i, a.whenToUpdBoxs[i]);
    data.setFloat("bodyfx"+i, a.setForceAtDur[i][0]);
    data.setFloat("bodyfy"+i, a.setForceAtDur[i][1]);
    String HiBox = "HiBox" + i;
    String HuBox = "HuBox" + i;
    data.setInt("HiLListSize" + i, a.HitBoxCollect.get(i).size());
    for(int j = 0; j < a.HitBoxCollect.get(i).size(); j++){
      data.setFloat(HiBox + "ax" + j, a.HitBoxCollect.get(i).get(j).addx);
      data.setFloat(HiBox + "ay" + j, a.HitBoxCollect.get(i).get(j).addy);
      data.setFloat(HiBox + "fx" + j, a.HitBoxCollect.get(i).get(j).forcex);
      data.setFloat(HiBox + "fy" +j, a.HitBoxCollect.get(i).get(j).forcey);
      data.setInt(HiBox + "br" +j, a.HitBoxCollect.get(i).get(j).br);
      data.setInt(HiBox + "ho" +j, a.HitBoxCollect.get(i).get(j).ho);
  }
    data.setInt("HuLListSize" + i, a.HurtBoxCollect.get(i).size());
    for(int j = 0; j < a.HurtBoxCollect.get(i).size(); j++){
      data.setFloat(HuBox + "ax" + j, a.HurtBoxCollect.get(i).get(j).addx);
      data.setFloat(HuBox + "ay" + j, a.HurtBoxCollect.get(i).get(j).addy);
      data.setInt(HuBox + "br" + j, a.HurtBoxCollect.get(i).get(j).br);
      data.setInt(HuBox + "ho" + j, a.HurtBoxCollect.get(i).get(j).ho);
    }
  }
  
  saveJSONObject(data, datnam);
}

Action loadActionData(String datnam){
  Action a = new Action();
  a.HitBoxCollect.clear();
  a.HurtBoxCollect.clear();
  
  JSONObject data = loadJSONObject(datnam);
    a.whenToUpdBoxs = new int[data.getInt("BoxsSize")];
    a.setForceAtDur = new float[data.getInt("BoxsSize")][2];
    
  for(int i = 0; i < data.getInt("BoxsSize"); i++){
      a.whenToUpdBoxs[i] =data.getInt("whenUpd"+i);
    a.setForceAtDur[i][0] = data.getFloat("bodyfx"+i);
    a.setForceAtDur[i][1] = data.getFloat("bodyfy"+i);
    String HiBox = "HiBox" + i;
    String HuBox = "HuBox" + i;
          a.HitBoxCollect.add(new ArrayList<ColCircle>());
    a.HurtBoxCollect.add(new ArrayList<ColCircle>());
    for(int j = 0; j < data.getInt("HiLListSize" + i); j++){   
      a.HitBoxCollect.get(i).add(new ColCircle());
      a.HitBoxCollect.get(i).get(j).addx = data.getFloat(HiBox + "ax" + j);
      a.HitBoxCollect.get(i).get(j).addy = data.getFloat(HiBox + "ay" + j);
      a.HitBoxCollect.get(i).get(j).forcex = data.getFloat(HiBox + "fx" + j);
      a.HitBoxCollect.get(i).get(j).forcey = data.getFloat(HiBox + "fy" + j);
      a.HitBoxCollect.get(i).get(j).br = data.getInt(HiBox + "br" + j);
      a.HitBoxCollect.get(i).get(j).ho = data.getInt(HiBox + "ho" + j);
  }
    for(int j = 0; j < data.getInt("HuLListSize" + i); j++){
      a.HurtBoxCollect.get(i).add(new ColCircle());
      a.HurtBoxCollect.get(i).get(j).addx = data.getFloat(HuBox + "ax" +j);
      a.HurtBoxCollect.get(i).get(j).addy = data.getFloat(HuBox + "ay" + j);
      a.HurtBoxCollect.get(i).get(j).br = data.getInt(HuBox + "br" + j);
      a.HurtBoxCollect.get(i).get(j).ho = data.getInt(HuBox + "ho" + j);
    }

  }
  return a;
}

boolean recPointCol(float x, float y, float x2, float y2, float br, float ho){
  
  return x >= x2 && x <= x2 + br && y >= y2 && y <= y2 + ho;
}

boolean recRecColCheck(float x, float y, float br, float ho, float x2, float y2, float br2, float ho2){
  
  return ((x <= x2 + br2 && x >= x2) || (x + br <= x2 +br2 && x + br >= x2) || (x <= x2 && x + br >= x2 +br2)) 
  && ((y <= y2 + ho2 && y >= y2) || (y + ho <= y2 +ho2 && y + ho >= y2) || (y <= y2 && y + ho >= y2 + ho2));
}

int sumOfArr(int[] arr){
  int sum = 0;
  for(int i = 0; i < arr.length; i++){
    sum += arr[i];
  }
  return sum;
}

int[] int_copyArrToSmallSize(int[] Arr, int size){
  int[] outArr = new int[size];
  for(int i = 0; i < size; i++){
    outArr[i] = Arr[i];
  }
   
  return outArr;
}

float[][] float_copyArrToSmallSize(float[][] Arr, int size){
    float[][] outArr = new float[size][2];
  for(int i = 0; i < size; i++){
    outArr[i][0] = Arr[i][0];
    outArr[i][1] = Arr[i][1];
  }
   
  return outArr;
}

PImage[] fillSprArr(int size, String datnam){
      PImage[] Sprites = new PImage[size];
    for(int i = 0; i < size; i++){
      Sprites[i] = loadImage(datnam + i + ".png");
    }
    return Sprites;
}

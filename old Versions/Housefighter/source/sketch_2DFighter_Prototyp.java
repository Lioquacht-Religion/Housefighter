import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.net.*; 
import net.java.games.input.*; 
import org.gamecontrolplus.*; 
import org.gamecontrolplus.gui.*; 
import ddf.minim.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class sketch_2DFighter_Prototyp extends PApplet {

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


Server s1, s2;
Client c1, c2;
String othersIp;







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

public void setup(){

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
  
  
  Camerabox = new ColRect(initWidth/2.0f, 0, initWidth, initHeight);
  
    frameRate(60);
  
  //fullScreen(P3D);
  
    EditForceXBox = new EintragBox(width-width/10, 100, 100, 100, 5);
  EditForceYBox = new EintragBox(width-width/10, 200, 100, 100, 5);
  EditDatnamBox = new EintragBox(width-width/8, 0, 100, 100, 5);
  
}
  
public void netcode(){
  
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
    Player2.inputs = PApplet.parseBoolean(split(input, ' '));  // Split values into an array
   
  }
    //c1.clear();
   
}
  
public void draw(){
  
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

public void selectScreen(){
      camera(initWidth/2, initHeight/2.0f, (initHeight/2.0f) / tan(PI*30.0f / 180.0f),
    initWidth/2, initHeight/2.0f, 0,
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

public Fighter chooseFighter(int index, int x, int y, char[] charinputs){
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
  
public void winScreen(){
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

public void stop(){
  minim.stop();
}


int eModeTimer = 0;
int eModeCurCBox = 0;
int eModeCurCAnim = 0;

public void editMode(Fighter EditPlayer, Action EditAction){
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
    int f = color(255);
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
        int f = color(255);
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
      EditPlayer.CurAction.whenToUpdBoxs[eModeCurCBox] = PApplet.parseInt(EditFrameDurBox.boxText);
      println("changed");
    }

  else if(click && recPointCol(mouseX, mouseY, PApplet.parseInt(EditPlayer.x)-300, PApplet.parseInt(EditPlayer.y)-400, 600, 600)){ 
    
    if(mouseButton == LEFT){
  if(editItemId == 0){
    EditPlayer.CurAction.HitBoxCollect.get(eModeCurCBox).add(new ColCircle(mouseX-EditPlayer.x, mouseY-EditPlayer.y, PApplet.parseInt(EditDiaBox.boxText), PApplet.parseInt(EditDiaBox2.boxText), PApplet.parseInt(EditForceXBox.boxText), PApplet.parseInt(EditForceYBox.boxText), -1));
    
  }
  else if(editItemId == 1){
    EditPlayer.CurAction.HurtBoxCollect.get(eModeCurCBox).add(new ColCircle(mouseX-EditPlayer.x, mouseY-EditPlayer.y, PApplet.parseInt(EditDiaBox.boxText), PApplet.parseInt(EditDiaBox2.boxText) ));
   
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

public void editModeKeyRe(Fighter EditPlayer){
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
    EditPlayer.CurAction.whenToUpdBoxs[0] = PApplet.parseInt(EditFrameDurBox.boxText);
    EditPlayer.CurAction.whenToUpdBoxs = int_copyArrToSmallSize(EditPlayer.CurAction.whenToUpdBoxs, EditPlayer.CurAction.HitBoxCollect.size());
    EditPlayer.CurAction.setForceAtDur = float_copyArrToSmallSize(EditPlayer.CurAction.setForceAtDur, EditPlayer.CurAction.HitBoxCollect.size());
    // TO DO: add same for force Array
  }
  else if(keyCode == RIGHT && editMode && EditPlayer.CurAction.HitBoxCollect.size() == EditPlayer.CurAction.whenToUpdBoxs.length){

    EditPlayer.CurAction.addAllLists( EditPlayer.CurAction.HitBoxCollect.size()-1 , PApplet.parseInt(EditFrameDurBox.boxText), 0, 0);
    println("addedList");
  }
  else if(keyCode == RIGHT && editMode && EditPlayer.CurAction.HitBoxCollect.size() < EditPlayer.CurAction.whenToUpdBoxs.length){
         EditPlayer.CurAction.addAllLists( EditPlayer.CurAction.HitBoxCollect.size()-1 , PApplet.parseInt(EditFrameDurBox.boxText), 0, 0);
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
public void keyPressed(){
  Player1.keyPressed();
  Player2.keyPressed();
}

public void keyReleased(){
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


public void mouseClicked(){
  click = true;
}

public void battleMode(){
  
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


public void battleModeVisuals(){
  
    if(frameFreeze <= 0){
  //centerX = initWidth/2.0;//dist(Player1.x, 0, Player2.x, 0)/2;
  float centerZ = 0;
  float centerY = 0;
  float centerZ2 = centerZ / tan(PI*30.0f / 180.0f);
  
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
  
  
    camera(Camerabox.x, initHeight/2.0f - centerY, (initHeight/2.0f+centerZ) / tan(PI*30.0f / 180.0f),
    Camerabox.x, initHeight/2.0f - centerY, 0,
    0, 1, 0);
    
  background(150);
  println("start drawing background");
    StageBackground.drawBackground();
    println("finished");
    //image(SBextra, 0, -200);
    //auslagern in fighter spezifische funktionen
    drawBar( PApplet.parseInt(Camerabox.x) - initWidth/4, 20 - PApplet.parseInt(centerY) , centerZ2, Player1.maxHP, Player1.curHP, -180);
    drawBar( PApplet.parseInt(Camerabox.x) + initWidth/4, 20 - PApplet.parseInt(centerY), centerZ2, Player2.maxHP, Player2.curHP, 180);
    drawBar( PApplet.parseInt(Camerabox.x) - initWidth/4, GROUNDHEIGHT+10 - PApplet.parseInt(centerY) , centerZ2, Player1.maxSuper, Player1.curSuper, -140);
    drawBar( PApplet.parseInt(Camerabox.x) + initWidth/4, GROUNDHEIGHT+10 - PApplet.parseInt(centerY), centerZ2, Player2.maxSuper, Player2.curSuper, 140);
    showInputs(Player1, Camerabox.x, 200);
    
    fill(0);
    textSize(30);
    
    if(Player1.comboCount > 0){
      text("COMBO:" + Player1.comboCount, PApplet.parseInt(Camerabox.x) - initWidth/2, 200 - PApplet.parseInt(centerY));
    }
    if( Player2.comboCount > 0){
      text("COMBO:" + Player2.comboCount, PApplet.parseInt(Camerabox.x) + initWidth/4, 200 - PApplet.parseInt(centerY));
    }
    pushMatrix();
    translate(0, 0, centerZ2);
    showInputbuffer(Player1, PApplet.parseInt(Camerabox.x) - initWidth/4, 20 - PApplet.parseInt(centerY));
    showInputbuffer(Player2, PApplet.parseInt(Camerabox.x) + initWidth/4, 20 - PApplet.parseInt(centerY));
    fill(0);
    rect(PApplet.parseInt(Camerabox.x) - initWidth/2 - 1, -centerY, PApplet.parseInt(Camerabox.x) - width/2 - initWidth, height);
    rect(PApplet.parseInt(Camerabox.x) + initWidth/2 + 2, -centerY, (width-initWidth)/2, height);
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

public void drawBar(int x, int y, float z, float maxBar, float curBar, float scale){
  float curBarLength = curBar / maxBar * scale;
  pushMatrix();
  translate(0, 0, z);
  fill(20, 70, 20);
  rect(x, y, scale, 20);
    fill(200, 30, 20);
  rect(x, y, curBarLength, 20);
  popMatrix();
}

public void showInputbuffer(Fighter Pl, int x, int y){
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

public void showInputs(Fighter F, float x, float y){
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

  
public boolean[] inputsKeyCode(boolean setBoolsTo, int[] keyCodes, boolean[] boolsToSet){
  for(int i = 0; i < keyCodes.length; i++){
      if(keyCode == keyCodes[i]){
    boolsToSet[i] = setBoolsTo;
  }
  }
  return boolsToSet;
}

public boolean[] inputsKey(boolean setBoolsTo, char[] keys, boolean[] boolsToSet){
  for(int i = 0; i < keys.length; i++){
      if(key == keys[i]){
    boolsToSet[i] = setBoolsTo;
  }
  }
  return boolsToSet;
}

public void saveActionData(Action a, String datnam){
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

public Action loadActionData(String datnam){
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

public boolean recPointCol(float x, float y, float x2, float y2, float br, float ho){
  
  return x >= x2 && x <= x2 + br && y >= y2 && y <= y2 + ho;
}

public boolean recRecColCheck(float x, float y, float br, float ho, float x2, float y2, float br2, float ho2){
  
  return ((x <= x2 + br2 && x >= x2) || (x + br <= x2 +br2 && x + br >= x2) || (x <= x2 && x + br >= x2 +br2)) 
  && ((y <= y2 + ho2 && y >= y2) || (y + ho <= y2 +ho2 && y + ho >= y2) || (y <= y2 && y + ho >= y2 + ho2));
}

public int sumOfArr(int[] arr){
  int sum = 0;
  for(int i = 0; i < arr.length; i++){
    sum += arr[i];
  }
  return sum;
}

public int[] int_copyArrToSmallSize(int[] Arr, int size){
  int[] outArr = new int[size];
  for(int i = 0; i < size; i++){
    outArr[i] = Arr[i];
  }
   
  return outArr;
}

public float[][] float_copyArrToSmallSize(float[][] Arr, int size){
    float[][] outArr = new float[size][2];
  for(int i = 0; i < size; i++){
    outArr[i][0] = Arr[i][0];
    outArr[i][1] = Arr[i][1];
  }
   
  return outArr;
}

public PImage[] fillSprArr(int size, String datnam){
      PImage[] Sprites = new PImage[size];
    for(int i = 0; i < size; i++){
      Sprites[i] = loadImage(datnam + i + ".png");
    }
    return Sprites;
}
abstract class Fighter{
  final boolean RIGHT = false;
  final boolean LEFT = true;
  
  PImage[] Sprs;
  
  ControlDevice device; ControlButton LightBut, MediumBut, HeavyBut, RCBut; ControlHat DPad; ControlSlider LStick_X, LStick_Y; boolean[] pInputs = new boolean[4];
  boolean[] inputs = new boolean[8]; //up , down, right, left, Roof, Mid, Base, utility(RC);
  boolean[] firstPressInp = new boolean[inputs.length];
  boolean[] firstPressDia = {true, true, true, true};
  int[] inputChargeT = new int[inputs.length];
  char[] charinputs;
  
  boolean AI_Controlled = false;
  boolean tint = false;
  
  IntList inputbufferDir = new IntList();
  ArrayList<IntList> inputbufferBut = new ArrayList<IntList>();
  int curInputTimer = 0;
  
  int comboCount = 0;
  
  int[] FBmotion = {2, 3, 6};
  int[] FBendBut = {0, 0, 10};
  int[] DPmotion = {6, 2, 3}; //{6, 3, 2, 3, 6};
  int[] fDashmotion = {6, 6};
  int[] bDashmotion = {4, 4};
  
  float x;
  float y;
  PVector Force = new PVector(0, 0);
  float gforce = 0;
  int dirMult = 1;
  float m = 1;
  
  ColRect CollisionBox;
  
  boolean facing = false;
  boolean grounded = false;
  boolean crouching = false;
  
  int maxHP = 200;
  int curHP = maxHP;
  int maxSuper = 1000;
  int curSuper = 0;
  int maxAirActions = 1;
  int curAirActions = maxAirActions;
  
  int HitStunT = 0;
  int cancelWindow = 0;
  
  ArrayList<ColCircle> HurtBoxes = new ArrayList<ColCircle>();
  ArrayList<ColCircle> HitBoxes = new ArrayList<ColCircle>();
  
  ArrayList<Projectile> Projectiles = new ArrayList<Projectile>();
  
  float[] AI_InputsScore = {.1f, .1f, .2f, .2f, .8f, .3f, .3f};
  int pHP = 200, pOppHP = 200; float abstand = 100;
  
  Animation CurAnimation = null;
  
  Action CurAction = new Action();
  
  Action Standing, Crouching;
  Action Knockdown, softKD;
  Action BeingGrapped;
  Action InHitStun;
  Animation HHit, LHit;
  Action Jumping, fDiaJump, bDiaJump, Jumping2, fDiaJump2, bDiaJump2;
  Action Blocking;
  Animation HBlock, LBlock;
  
Action fWalk, bWalk;

Action FDash, BDash;

Action EditAction;

  Action LightNormal, MidNormal2, HeavyNormal, cr_LightNormal, cr_MidNormal, cr_HeavyNormal, j_LightNormal, j_MidNormal, j_HeavyNormal;

Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, FDash, BDash, Jumping, fDiaJump, bDiaJump},
  {cr_LightNormal, cr_MidNormal, cr_HeavyNormal},
  {j_LightNormal, j_MidNormal, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2},
  {}
};

  ArrayList<ArrayList<Action>> ActionList = new ArrayList<ArrayList<Action>>();
  ArrayList<Action> st_Actions, cr_Actions, j_Actions, sp_Actions;
  
  public void fillActionsList(Action[][] ActTab){
    for(int i = 0; i < ActTab.length; i++){
      for(int j = 0; j < ActTab[i].length; j++){
        ActionList.get(i).set(j, ActTab[i][j]);
      }
    }
    
  }
  
  public void AIControll2(Fighter Opp){
    float aggroLevel = dist(x, y, Opp.x, Opp.y)*0.1f + (Opp.maxHP/(Opp.curHP+1)) - (curHP/(maxHP+1));
    float attChance, moveChance = dist(x, y, Opp.x, Opp.y);
    //Attproperties needed
    int condAttKind = CurAction.MID;
    int condAttTime;
    
    if(whichDirHold(Opp, -1) || Opp.CurAction == Opp.Blocking ){  //Check Enemy Blocking
      if(Opp.inputs[1]){
        condAttKind = CurAction.HIGH;
        inputs[1] = false;
        AI_InputsScore[0] += 0.1f;
      }
      else{ condAttKind = CurAction.LOW; inputs[1] = true; AI_InputsScore[1] += 0.1f;}
      
    }
    
    if(aggroLevel > 70){
      for(int i = 4; i < 7; i++){
        if(moveChance <= 120 * (i-3)){
          AI_InputsScore[i] += 0.1f;
        }
      }
    }
    
    if(aggroLevel > 50 && moveChance > 250){
      inputs[horDir(1)] = true;
      inputs[1] = false;
      AI_InputsScore[1] -= 0.05f;
      AI_InputsScore[0] += 0.1f;
      AI_InputsScore[horDir(1)] += 0.1f;
      AI_InputsScore[horDir(-1)] -= 0.05f;
    }
    else{      inputs[horDir(-1)] = true;
      AI_InputsScore[horDir(-1)] += 0.1f;  AI_InputsScore[horDir(1)] -= 0.05f;}
    
    //search for fitting and for state usable attack
          if((CurAction == Standing || CurAction == bWalk|| CurAction == fWalk || (cancelWindow > 0 && !inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){   
            if(moveChance > 600 && aggroLevel > 60 && FDash != null){ changeAction(FDash);}
       }
       else if( (CurAction == Crouching || (cancelWindow > 0 && inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){
    
         
       }
       else if( (CurAction == Standing || cancelWindow > 0) && y < GROUNDHEIGHT && !CollisionBox.bottom){
         if(moveChance > 600 && aggroLevel > 60 && FDash != null){ changeAction(FDash);}
       }
       
   // when in Hitstun
   if(CurAction == InHitStun){
     inputs[horDir(-1)] = true;
     AI_InputsScore[horDir(-1)] += 0.1f;
     if(Opp.CurAction.attKind == CurAction.LOW){
       inputs[1] = true;
       AI_InputsScore[1] += 0.1f;
     }
     else if(Opp.CurAction.attKind == CurAction.HIGH || Opp.CurAction.attKind == CurAction.AIR){
              inputs[1] = false;
       AI_InputsScore[1] -= 0.1f;
     }
   }
   
     
  }
  
  public int horDir(int mult){
    if(this.dirMult * mult == 1){
      return 2;
    }
    
    return 3;
  }
  
  public boolean whichDirHold(Fighter Opp, int mult){
    
    return (Opp.dirMult == 1*mult && Opp.inputs[2]) ||(Opp.dirMult == -1*mult && Opp.inputs[3]);
  }
  
    public void AI_Controll(Fighter Opp){
    if(CurAction == Standing || CurAction == Crouching){
    for(int i = 0; i < AI_InputsScore.length; i++){
      if(AI_InputsScore[i] < 1.f && inputs[i] && (dist(x, y, Opp.x, Opp.y) < abstand-5 || curHP > pHP) ){
        AI_InputsScore[i] += 0.1f;
      }
      else if(AI_InputsScore[i] < 1.f && inputs[i] && Opp.curHP < pOppHP){
        AI_InputsScore[i] += 0.1f;
      }
      else if(AI_InputsScore[i] > 0 && inputs[i]){
             AI_InputsScore[i] -= 0.1f; 
           }
           
               if(AI_InputsScore[i] > 1.f){
                 AI_InputsScore[i] = 0.4f;
    }
    }
    
    }
     if( AI_InputsScore[horDir(1)] < 3 && curHP < pHP){
             AI_InputsScore[0] += 0.1f;
             AI_InputsScore[horDir(1)] += 0.1f;
             AI_InputsScore[4] += 0.1f;
             AI_InputsScore[5] += 0.1f;
             AI_InputsScore[6] += 0.1f;
           }
    
   String data = "";
    for(int i = 0; i < AI_InputsScore.length; i++){
      if( random(0, 1) < AI_InputsScore[i] ){
        inputs[i] = true;
      }
      else { inputs[i] = false; }
      
      data += AI_InputsScore[i] + ":";
    }
    println(data);
    
         for(int i = 0; i < firstPressInp.length; i++){
      if(!inputs[i]){
        firstPressInp[i] = true;
      }
    }
    
    
      if( !compareBoolArrs(pInputs, inputs) ){
        poop(true, 0, 0, 3);
        poop(true, 1, 0, 2);
        poop(true, 2, 1, 3);
        poop(true, 3, 1, 2);
      }
    
     for(int i = 0; i < pInputs.length; i++){
      pInputs[i] = inputs[i];
    }
    
    
    abstand = dist(x, y, Opp.x, Opp.y);
    pHP = curHP;
    pOppHP = Opp.curHP;
  }
  
  Fighter(){
  }
  
  Fighter(int x, int y, char[] charinputs){
    this.x = x;
    this.y = y;
    this.charinputs = charinputs;
  }
  
  Fighter(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    this(x, y, charinputs);
    this.tint = tint;
    this.AI_Controlled = AI_Controlled;
  }
  
    Fighter(int x, int y,  char[] charinputs, ControlDevice device){
    this.x = x;
    this.y = y;  
    this.charinputs = charinputs;
    if(device != null){
    this.device = device;
    setConDevice();
    }
  }
  
  public void setConDevice(){
    DPad = device.getHat("DPAD");
    LightBut = device.getButton("LIGHT");
    MediumBut = device.getButton("MEDIUM"); 
    HeavyBut = device.getButton("HEAVY");
    RCBut = device.getButton("RC");
    LStick_X = device.getSlider("LSTICK-X"); 
    LStick_Y = device.getSlider("LSTICK-Y");
  }
  
  
  public void setup(){
    
        int[] ani10 = {2};
  InHitStun = new Action(ani10, 0, 0, 0, 0, true, true, false, false);
  InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160));
  
  int[] ani11 = {11, 11};
  Knockdown = new Action(ani11, 0, 0, 0, 0, true, true, false, false);
  //Knockdown.gravMult = 0.92;
  Knockdown.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.updFrameDataArr(0, 1); 
  Knockdown.updFrameDataArr_float(0, 0, 0);
  Knockdown.addAllLists(1, 40, 0, 0);
   
   int[] ani13 = {2,2};
  softKD = new Action(ani13,
  0, 0, 0, 0, true, true, false, false);
  softKD.gravMult = 0.92f;
  softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  softKD.updFrameDataArr(0, 1); 
  softKD.updFrameDataArr_float(0, 0, 0);
  softKD.addAllLists(1, 40, 0, 0);
  
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 200, 160, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
  specSetup();
  
  }
  
  public void specSetup(){
  }
  
  public void standingStateReturn(Fighter Opp){
    if(y == GROUNDHEIGHT || CollisionBox.bottom){
      curAirActions = maxAirActions;
    }
    
              if((y == GROUNDHEIGHT || CollisionBox.bottom) && inputs[1]){
        CurAction.playAction2(this, Opp, Crouching);
      }
      else if((y >= GROUNDHEIGHT || CollisionBox.bottom) && CurAction.attKind == 4){
        changeAction(Crouching);
      }
      else{
        CurAction.playAction2(this, Opp, Standing);
      }
  }
  
  private void gameLogic(Fighter Opp){
      deviceInput();
    
    if(AI_Controlled && millis() % 10 == 0){
      
      AI_Controll(Opp);
      AIControll2(Opp);
    }
    //chargeinputs
    for(int i = 0; i < inputs.length; i++){
      if(inputs[i] && inputChargeT[i] <= 1000){ 
        inputChargeT[i]++;
      }
      else if(inputChargeT[i] > 10){
        inputChargeT[i] -= inputChargeT[i]/10;
      }
      else{inputChargeT[i] = 0;}
      
    }

   //chargeinputs
   
   for(int i = 0; i < firstPressInp.length; i++){
      if(!inputs[i]){
        firstPressInp[i] = true;
      }
    }
    
     if( !compareBoolArrs(pInputs, inputs) ){
        poop(true, 0, 0, 3);
        poop(true, 1, 0, 2);
        poop(true, 2, 1, 3);
        poop(true, 3, 1, 2);
      }
        for(int i = 0; i < pInputs.length; i++){
      pInputs[i] = inputs[i];
    }
    
    standingStateReturn(Opp);
    
    
    fillInputBuffer();
    controll();
   
    updateColList();
    updateProjectiles(Opp);
    
    if(CurAnimation != null){
      CurAnimation.handleAnim();
        }
    
    
        curHP = constrain(curHP, 0, maxHP);
    curSuper = constrain(curSuper, 0, maxSuper);
    x = constrain(x, Camerabox.x - Camerabox.br/2, Camerabox.x + Camerabox.br/2);
    
    if(cancelWindow > 0){
      cancelWindow--;
    }
    
   extraStuff();
  }
 PImage[] last3Spr = new PImage[4];
 float[][] l3S_XY = new float[last3Spr.length][2];
 
  private void draw(){
    
    //sprite();
        
    pushMatrix();
    translate(x, y);
    if(facing == LEFT){
      scale(-1, 1);
    }
    
    if(tint){
    tint(240, 153, 255);
    }
    
    imageMode(CENTER);
    if(CurAnimation != null){
      
          if(tint){
    tint(240, 153, 255);
    }
      image( CurAnimation.Sprites[CurAnimation.curCollumn],
      CurAnimation.X_coords, 
      0 - CurAnimation.Sprites[CurAnimation.curCollumn].height/2 + CurAnimation.Y_coords);   
      
      
    }
    else{
        image(Sprs[CurAction.sprsIds[CurAction.curCollumn]], 0, 0 - Sprs[CurAction.sprsIds[CurAction.curCollumn]].height/2);
    }
    tint(255);
    popMatrix();
 
    /*fill(0, 0, 200, 90);
    drawColList(HurtBoxes);
    fill(200, 0, 0, 90);
    drawColList(HitBoxes);*/

    for(int i = Projectiles.size()-1; i >= 0; i--){
      Projectiles.get(i).draw();
    }

  }
  
  public void extraStuff(){
  }
  
  public boolean compareBoolArrs(boolean[] boolArr1, boolean[] boolArr2){
    for(int i = 0 ; i < boolArr1.length; i++){
      if(boolArr1[i] != boolArr2[i]){
        return false;
      }
    }
    
    return true;
  }
  
  private void deviceInput(){
    if(device != null){
      inputs[0] = DPad.up(); 
      inputs[1] = DPad.down(); 
      inputs[2] = DPad.right(); 
      inputs[3] = DPad.left();
      if(LStick_Y.getValue() >= 0.6f){
        inputs[1] = true;
        inputs[0] = false;
      }
      else if(LStick_Y.getValue() <= -0.6f){
        inputs[0] = true;
        inputs[1] = false;
      }
            if(LStick_X.getValue() >= 0.6f){
        inputs[2] = true;
        inputs[3] = false;
      }
      else if(LStick_X.getValue() <= -0.6f){
        inputs[3] = true;
        inputs[2] = false;
      }
      LStick_X.getValue(); 
      LStick_Y.getValue();
      inputs[4] = LightBut.pressed(); 
      inputs[5] = HeavyBut.pressed(); 
      inputs[6] = MediumBut.pressed(); 
      inputs[7] = RCBut.pressed();
    }
    
    
  }
  
  public void keyPressed(){
    inputs = inputsKey(true, charinputs, inputs);
  }
  
  //fast Dash issue, weird reseting of input bools
  public void poop(boolean setTo, int index, int n1, int n2){
    if(setTo && (!inputs[n1] || !inputs[n2])){
      firstPressDia[index] = setTo;
      firstPressInp[n1] = true;
      firstPressInp[n2] = true;
    }
    else if(!setTo && inputs[n1] && inputs[n2]){
      firstPressDia[index] = setTo;
      firstPressInp[n1] = false;
      firstPressInp[n2] = false;
    }
  }
  
  public void keyReleased(){
    
    if(!AI_Controlled){
      inputs = inputsKey(false, charinputs, inputs);
    
    }
    
  }
  
public void fighterActionsExtra(){}

public void fighterActions(){
      if((CurAction == Standing || CurAction == bWalk|| CurAction == fWalk || (CurAction.hitCancel && cancelWindow > 0 && !inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){   
           normalWalk();
           jump();
           dash();    
           st_Normals();
    
       }
       else if( (CurAction == Crouching || (CurAction.hitCancel && cancelWindow > 0 && inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){
         cr_Normals();
    
         
       }
       else if( (CurAction == Standing || (CurAction.hitCancel && cancelWindow > 0) ) && y < GROUNDHEIGHT && !CollisionBox.bottom){
         dash();
         jump();
         j_Normals();
         
       }
       
       fighterActionsExtra();
}

public void st_Normals(){}; public void cr_Normals(){}; public void j_Normals(){};


public void normalWalk(){
      if(inputs[2]){
      if(facing == RIGHT){
        CurAction.reset();
        CurAction = fWalk;
        curSuper++;
      }
      else{
        CurAction.reset();
        CurAction = bWalk;
      }
    }
    
    if(inputs[3]){
       if(facing == LEFT){
        CurAction.reset();
        CurAction = fWalk;
        curSuper++;
      }
      else{
        CurAction.reset();
        CurAction = bWalk;
      }
    }
}
public void jump(){
  
  int[] fJump = {9};
  int[] bJump = {7};
  Action l_ResultAction = CurAction;
  if(y == GROUNDHEIGHT || CollisionBox.bottom){
    if(compareBufferWithCombAtt(fJump)){
      l_ResultAction = fDiaJump;
    }
    else
    if(compareBufferWithCombAtt(bJump)){
      l_ResultAction = bDiaJump;
    }
    else
      if(inputs[0] && firstPressInp[0]
      ){
      l_ResultAction = Jumping;
    }
    
  }
  else
  if(curAirActions > 0){
        if(compareBufferWithCombAtt(fJump)){
      l_ResultAction = fDiaJump2;
    }
    else
    if(compareBufferWithCombAtt(bJump)){
      l_ResultAction = bDiaJump2;
    }
    else
      if(inputs[0] && firstPressInp[0]){
      l_ResultAction = Jumping2;
    }
  }
  
  if(l_ResultAction == Jumping || l_ResultAction == fDiaJump || l_ResultAction == bDiaJump || l_ResultAction == Jumping2 || l_ResultAction == fDiaJump2 || l_ResultAction == bDiaJump2){
           VisEffectsList.add(new VisualEffect(CollisionBox.x + CollisionBox.br/2, GROUNDHEIGHT - 125, dustJumpEff, 0));
       Soundeffects[2].cue(0);
       Soundeffects[2].play();
       if(inputbufferDir.size()-1 > 0){
       inputbufferDir.set(inputbufferDir.size()-1, 5);
       }
       curAirActions--;
       changeAction(l_ResultAction);
    }
  
}
public void dash(){
  if(curAirActions > 0){
          if(compareBufferWithCombAtt(fDashmotion)){
            CurAction.reset();
          CurAction = FDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
    if(compareBufferWithCombAtt(bDashmotion)){
            CurAction.reset();
          CurAction = BDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
  }
}
  
  public void controll(){ 
  
        if(HitStunT == 0 && CurAction != Blocking && CurAction != BeingGrapped && CurAction != InHitStun){
    
       
    if(inputs[1] && (y == GROUNDHEIGHT || CollisionBox.bottom) && !inputs[0] && CurAction == Standing){
      crouching = true;
      CurAction.reset();
      CurAction = Crouching;
    }
    else{
      crouching = false;
    }
    
       fighterActions();
       
    if( CurAction != Standing && inputs[7] && firstPressInp[7] && curSuper >= 50){
      changeAction(Standing);
      curSuper -= 50; slowMoDur = 40; slowMoValue = 4;
      println("RomanCancel");
      Soundeffects[1].cue(0);
      Soundeffects[1].play();
      VisEffectsList.add(new VisualEffect(x, y, RCEff, 0));
    }
        
        }    
        else if(HitStunT > 0){
      HitStunT--;
    }
    
        if(CurAction == Knockdown && Knockdown.curCollumn == 1){
      for(int i = 4; i < inputs.length; i++){
              if(inputs[i] && firstPressInp[i]){
                CurAction.reset();
                CurAction = Standing;
                      Soundeffects[1].cue(0);
      Soundeffects[1].play();
      VisEffectsList.add(new VisualEffect(x, y, RCEff, 0));
                break;
              }
    }
    }
    
            if(CurAction == softKD && softKD.curCollumn == 1 && y < GROUNDHEIGHT){
      for(int i = 4; i < inputs.length; i++){
              if(inputs[i] && firstPressInp[i]){
                CurAction.reset();
                CurAction = Standing;
                      Soundeffects[1].cue(0);
      Soundeffects[1].play();
      VisEffectsList.add(new VisualEffect(x, y, RCEff, 0));
                break;
              }
    }
    }
    else if(CurAction == softKD && softKD.curCollumn == 1 && y >= GROUNDHEIGHT){
                CurAction.reset();
                CurAction = Standing;
    }
    
  
    if(-0.1f > Force.x || Force.x > 0.1f){
      x += Force.x;
    }
    if(!CollisionBox.bottom){
      y += Force.y;
    }
    
    if(CurAction.gravityActive && !CollisionBox.bottom){
      Force.y += gforce;
    }
    else{gforce = 0;}
    
    if( y < GROUNDHEIGHT && CurAction.gravityActive && !CollisionBox.bottom && gforce <= 3
    ){
      gforce += 0.2f * m + 0.01f * comboCount;
      gforce *= CurAction.gravMult;
      //println("grav:" + gforce);
    }
    else if(y > GROUNDHEIGHT){
      y = GROUNDHEIGHT;
      Force.y = 0;
      gforce = 0;
    }
        if(Force.x > 0){
      Force.x = Force.x - 0.1f;
    }else
    if(Force.x < 0){
      Force.x = Force.x + 0.1f;
    }
    
     
    
    for(int i = 0; i < firstPressInp.length; i++){
      if(inputs[i]){
        firstPressInp[i] = false;
      }
    }
    
    poop(false, 0, 0, 3);
    poop(false, 1, 0, 2);
    poop(false, 2, 1, 3);
    poop(false, 3, 1, 2);

  }
  
    public void fillInputBuffer(){
    /*
    789
    456  10/Roof ; 11/Mid ; 12/Base
    123
    */
    int inputId = 5;
    if(inputs[0] && inputs[2] && firstPressDia[1]
    ){
      inputId = 9;
    }
    else if(inputs[0] && inputs[3] && firstPressDia[0]){
      inputId = 7;
    }
    else if(inputs[1] && inputs[2] && firstPressDia[3]){
      inputId = 3;
    }
    else if(inputs[1] && inputs[3] && firstPressDia[2]){
      inputId = 1;
    }
    else{
      if(inputs[0] && firstPressInp[0]){
        inputId = 8;
      }
      else if(inputs[1] && firstPressInp[1]){
        inputId = 2;
      }
      else if(inputs[2] && firstPressInp[2]){
        inputId = 6;
      }
      else if(inputs[3] && firstPressInp[3]){
        inputId = 4;
      }
      
    }
    
    IntList l_ButtonInputs = new IntList();

   if(inputs[4] && firstPressInp[4]){
     l_ButtonInputs.append(10);
   }
   if(inputs[5] && firstPressInp[5]){
     l_ButtonInputs.append(11);
   }
    
    if(inputId != 5){
      curInputTimer = 0;
       if(facing == LEFT){
          int l_Dir = inputId;
          if(l_Dir == 1 || l_Dir == 4 || l_Dir == 7){
            l_Dir += 2;
          }
          else if(l_Dir == 3 || l_Dir == 6 || l_Dir == 9){
            l_Dir -= 2;
          }
          inputId = l_Dir;
        }
      inputbufferDir.append(inputId);
      inputbufferBut.add(l_ButtonInputs);
    }
    
    if(inputbufferDir.size() > 15){
      inputbufferDir.remove(0);
    }
   
       if(inputbufferBut.size() > 15){
      inputbufferBut.remove(0);
    }
    
    if(curInputTimer < 10){
    curInputTimer++;
    }
    else{
      inputbufferDir.clear();
      inputbufferBut.clear();
      curInputTimer = 0;
    }

  }
  
  public void sprite(){
    fill(0, 200, 0);
    CollisionBox.x = x - CollisionBox.br/2;
    CollisionBox.y = y;
    CollisionBox.draw();
    fill(200, 200, 0, 90);
    if(facing == RIGHT){
      triangle(x, y, x, y+40/2, x+40, y);
    }
    else{
      triangle(x, y, x, y+40/2, x-40, y);
    }
    line(x, y, x + Force.x*2, y + Force.y*2);
  }
  
  public void drawColList(ArrayList<ColCircle> ColList){
    for(ColCircle c : ColList){
      c.draw(dirMult);
    }
  }
  
  /*void updColListWithActList(int i){
    HitBoxes.clear();
     for(int j = 0; i < EditAction.HitBoxCollect.get(i).size(); j++){
       HitBoxes.add(new ColCircle(EditAction.HitBoxCollect.get(i).get(j)));
       HitBoxes.get(j).addx *= dirMult;
       HitBoxes.get(j).forcex *= dirMult;
     }
     HurtBoxes.clear();
     for(int j = 0; i < EditAction.HurtBoxCollect.get(i).size(); j++){
       HurtBoxes.add(new ColCircle(EditAction.HurtBoxCollect.get(i).get(j)));
       HurtBoxes.get(i).addx *= dirMult;
       HurtBoxes.get(i).forcex *= dirMult;
     }
  }*/
  
  public void updateColList(){
    for(ColCircle c : HurtBoxes){
      c.setxy(x, y);
    }
    for(int i = HitBoxes.size() - 1; i >= 0; i--){
      HitBoxes.get(i).setxy(x, y);
      if(HitBoxes.get(i).exTimer != -1 && HitBoxes.get(i).exTimer >= 1){
        HitBoxes.get(i).exTimer--;
      }
      else if(HitBoxes.get(i).exTimer == 0){
        HitBoxes.remove(i);
      }
    }
    
  }
  
 public void changeXdirOfBoxes(){
        for(ColCircle c : HurtBoxes){
          c.addx *= -1;
          c.forcex *= -1;
    }
    for(ColCircle c : HitBoxes){
      c.addx *= -1;
      c.forcex *= -1;
    }
  }
  
  public void facingCheckAndChange(Fighter Opp){
    if(CurAction == Standing || CurAction == Crouching){
    if(facing == RIGHT && Opp.x < x ){
      dirMult = -1;
      facing = LEFT;
      println("left");
          float l = AI_InputsScore[2];
    AI_InputsScore[2] = AI_InputsScore[3];
    AI_InputsScore[3] = l;
      
    }
    else if(facing == LEFT && Opp.x > x ){
      dirMult = 1;
      facing = RIGHT;
      println("right");
                float l = AI_InputsScore[2];
    AI_InputsScore[2] = AI_InputsScore[3];
    AI_InputsScore[3] = l;
    }
    }

  }
  
  public Action operationsOnHit(Fighter Opp, ColCircle[] l_Boxes){
    Action resultAction = Opp.CurAction;
    
    if(l_Boxes != null){
    ColCircle HitBox = l_Boxes[0], c = l_Boxes[1];
    
     if( checkBlock(Opp, CurAction.attKind)
        ){
          //Opp.CurAction.reset();
          resultAction = Opp.Blocking;
          Force.x += -HitBox.forcex*2*dirMult + (CurAction.damage * -dirMult);
          //Force.y = -HitBox.forcey;
          Opp.Force.x = HitBox.forcex*dirMult + (CurAction.damage/10 + comboCount) * dirMult;
          //Opp.Force.y = HitBox.forcey / 10;
          Opp.Blocking.whenToUpdBoxs[0] = CurAction.affBlockStunT;
          
          if(Opp.inputs[1]){
            resultAction.AttAnim = Opp.LBlock;
            resultAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            resultAction.AttAnim = Opp.HBlock;
            resultAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }
          
          if(CurAction.firstHit){
            Opp.curHP -= CurAction.damage/10;
            CurAction.specialEffectOnHit(this, Opp);
            VisEffectsList.add(new VisualEffect(Opp.x + c.addx + c.br/2 * Opp.dirMult, y + HitBox.addy, BlockEff, CurAction.damage/4));
                Soundeffects[4].cue(0);
                Soundeffects[4].play();
          }
          
          if(!CurAction.multiHit){
            CurAction.firstHit = false;
          }
          frameFreeze = CurAction.damage/10;
  }
  
    else
      if(CurAction != BeingGrapped){
        Force.x =  (CurAction.damage/2 + comboCount) * -dirMult;
        Opp.Force.x = HitBox.forcex*dirMult + (CurAction.damage/4 + comboCount) * dirMult;
        Opp.Force.y = HitBox.forcey;
        Opp.HitStunT = CurAction.affHitStunT / (comboCount/2+1);
        

        
        if(Opp.y+10 < GROUNDHEIGHT && Opp.CurAction != Opp.softKD && Opp.CurAction != Opp.Knockdown){
          resultAction = Opp.softKD;
          Opp.softKD.whenToUpdBoxs[0] = CurAction.affHitStunT;
        }
        else if(CurAction.knocksDown && Opp.CurAction != Opp.Knockdown){
          resultAction = Opp.Knockdown;
          Opp.Knockdown.whenToUpdBoxs[0] = 20;//CurAction.affHitStunT;
        }
        
        else if(Opp.CurAction != Opp.Knockdown && Opp.CurAction != Opp.softKD){
          resultAction = Opp.InHitStun;
          Opp.InHitStun.whenToUpdBoxs[0] = CurAction.affHitStunT;
                            if(Opp.inputs[1]){
            resultAction.AttAnim = Opp.LHit;
            resultAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            resultAction.AttAnim = Opp.HHit;
            resultAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }
        }
        

        
        if(CurAction.firstHit){
          Opp.curHP -= CurAction.damage / (comboCount/2+1);
          curSuper += CurAction.damage;
          Opp.curSuper += CurAction.damage/2;
          CurAction.specialEffectOnHit(this, Opp);
             comboCount++;
             cancelWindow = 6;
          VisEffectsList.add(new VisualEffect(Opp.x + c.addx + c.br/2 * Opp.dirMult, y + HitBox.addy, HitEff, CurAction.damage/4));
                Soundeffects[0].cue(0);
                Soundeffects[0].play();
        }
        
        if(!CurAction.multiHit){
          CurAction.firstHit = false;
        }
        frameFreeze = CurAction.damage/10;
      }
    }
    
    return resultAction;
  }
  
  public ColCircle[] checkHit(Fighter Opp){
      if(Opp.CurAction != Opp.InHitStun && Opp.CurAction != Opp.BeingGrapped && Opp.CurAction != Opp.Knockdown && Opp.CurAction != Opp.softKD){
             comboCount = 0;
           }
     
    ColCircle[] l_Boxes = null;

    for(ColCircle HitBox : HitBoxes){
    for(ColCircle c : Opp.HurtBoxes){

      if(HitBox.compare(c, dirMult, Opp.dirMult)){
        l_Boxes = new ColCircle[2];
        l_Boxes[0] = HitBox;
        l_Boxes[1] = c;
          
          break;
        }
        
        
      }
    }
 
    //Projektile //TO DO: Blocken hinzufügen
        for(int indexProj = Projectiles.size()-1; indexProj >= 0; indexProj--){
          Projectile p = Projectiles.get(indexProj);
    for(ColCircle c : Opp.HurtBoxes){
      if(p.HitBox.compare(c, 1, 1)){  
                if( checkBlock(Opp, p.attKind)    
                ){
          Opp.CurAction.reset();
          Opp.CurAction = Opp.Blocking;
          Opp.Force.x += p.forcex / 4;
          Opp.Force.y += p.forcey / 4;
          Opp.Blocking.whenToUpdBoxs[0] = p.hitStun/2;
          Opp.curHP -= CurAction.damage/10;
           VisEffectsList.add(new VisualEffect(p.x, p.y, BlockEff, p.damage/4));
                Soundeffects[0].cue(0);
                Soundeffects[0].play();
                
                    if(Opp.inputs[1]){
            Opp.CurAction.AttAnim = Opp.LBlock;
            Opp.CurAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            Opp.CurAction.AttAnim = Opp.HBlock;
            Opp.CurAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }

         if(p.destroyedByHit){
                     if(p.destrEff != null){
                        VisEffectsList.add(new VisualEffect(p.x, p.y, p.destrEff, 1));  
                        }
          Projectiles.remove(indexProj);

        }
          break;
        }
        else{
                Opp.Force.x += p.HitBox.forcex;
        Opp.Force.y += p.HitBox.forcey;
        Opp.curHP -= p.damage;
        Opp.HitStunT = p.hitStun;
        p.specialOnHit(this, Opp);
        comboCount++;

        if(Opp.y+10 < GROUNDHEIGHT){
          Opp.CurAction = Opp.softKD;
          Opp.Knockdown.whenToUpdBoxs[0] = p.hitStun;
        }
        else{
          Opp.CurAction.reset();
          Opp.CurAction = Opp.InHitStun;
          if(Opp.inputs[1]){
            Opp.CurAction.AttAnim = Opp.LHit;
            Opp.CurAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            Opp.CurAction.AttAnim = Opp.HHit;
            Opp.CurAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }
        }
        
           VisEffectsList.add(new VisualEffect(p.x, p.y, HitEff, p.damage/4));
                Soundeffects[0].cue(0);
                Soundeffects[0].play();
        if(p.destroyedByHit){
                 if(p.destrEff != null){
                   VisEffectsList.add(new VisualEffect(p.x, p.y, p.destrEff, 1));  
                 }
          Projectiles.remove(indexProj);
        }
        break;
      }
      }
    }
        }
        
        return l_Boxes;
      
  }
  
  public boolean checkBlock(Fighter Opp, int attKind){
    return ((Opp.facing == LEFT && Opp.inputs[2]) || (Opp.facing == RIGHT && Opp.inputs[3]))
        && (
        ( (Opp.CurAction == Opp.Standing || Opp.CurAction == Opp.bWalk)
        && (attKind == CurAction.HIGH || attKind == CurAction.AIR || attKind == CurAction.MID || attKind == CurAction.NOTHING))
        || 
        ( (Opp.CurAction == Opp.Crouching ) 
        && (attKind == CurAction.LOW || attKind == CurAction.MID || attKind == CurAction.NOTHING))
        )
        
        ||
        
        (Opp.CurAction == Opp.Blocking 
        && (
        (attKind == CurAction.MID || attKind == CurAction.NOTHING)
        ||
        (!Opp.inputs[1] && (attKind == CurAction.HIGH || attKind == CurAction.AIR) )
        || 
        (Opp.inputs[1] && attKind == CurAction.LOW )
        ) 
        );
  }
  
  public void updateProjectiles(Fighter Opp){
   for(int i = Projectiles.size()-1; i >= 0; i--){
      Projectile p = Projectiles.get(i);
      p.gameLogic(this, Opp);
      
      if(Projectiles.get(i).exTimer == 0){
               if(p.destrEff != null){
                  VisEffectsList.add(new VisualEffect(p.x, p.y, p.destrEff, 1));  
               }
        Projectiles.remove(i);
      }
    }
  }
  
   public boolean compareBufferWithCombAtt(int[] dirsOfAtt){
    IntList DirBuffer = inputbufferDir;
    
    if(DirBuffer.size() < dirsOfAtt.length){
      return false;
    }
    else{
      for(int i = 0; i < dirsOfAtt.length; i++){
     
 
        if(facing == RIGHT){
          
        if(DirBuffer.get(DirBuffer.size() - dirsOfAtt.length + i) == dirsOfAtt[i]){
         
        }
        else{
          return false;
        }
        
        
        }
        
        else if(facing == LEFT){
          int l_Dir = DirBuffer.get(DirBuffer.size() - dirsOfAtt.length + i);
          /*if(l_Dir == 1 || l_Dir == 4 || l_Dir == 7){
            l_Dir += 2;
          }
          else if(l_Dir == 3 || l_Dir == 6 || l_Dir == 9){
            l_Dir -= 2;
          }*/
          //DirBuffer.set(l_Dir, DirBuffer.size() - dirsOfAtt.length + i);
        if( l_Dir == dirsOfAtt[i]){
         
        }
        else{
          return false;
        }
        }
      
      }
      
    }
    
    return true;
  }
  
 
  public void changeAction(Action a){
    CurAction.reset();
    CurAction = a;
    if(a.AttAnim != null){
      CurAnimation = a.AttAnim;
      CurAnimation.timer = 0; CurAnimation.curCollumn = 0;
    }
    else{ CurAnimation = null;}
  }
 
  
}//Ende Hauptklasse

//FAMILIENHAUS###############################################################################################################################################################################################
class F_FHaus extends Fighter{

  Action st_fHeavy;
  Action BaseNormal;
  Action RoofNormal;
  ProjAction Fireball;
  ProjAction Fireball2;
  Action Special1;
  Action Special3;
  Action Special4;
  Animation FB1AnimUp, FB1AnimMid, FB1AnimDown, BettDestrEff;
  
  F_FHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_FHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
    F_FHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
public void specSetup(){
   CollisionBox = new ColRect(0, 0, 150, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
    int[] etimes0 = {10, 10, 10};
    int[] etimes1 = {3, 3, 5, 5, 3, 3, 3, 3};
  FB1AnimUp = new Animation(etimes0, 0, 0, 3, "Projectiles/Bett/FH-BettUp");
  FB1AnimMid = new Animation(etimes0, 0, 0, 3, "Projectiles/Bett/FH-BettMid");
  FB1AnimDown = new Animation(etimes0, 0, 0, 3, "Projectiles/Bett/FH-BettDown");
  BettDestrEff = new Animation(etimes1, 0, 0, 8, "Effekte/BettDestrEff/FH-BettEff");
  
  int[] times0 = {5, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
  
      
  int x1 = 0;
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, x1, 7, "FHouse/FH-fWalk/FH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 0, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2, 0);
  
  int x3 = 0;
  int[] times3 = {1, 2, 1, 4};
  Animation Anim3 = new Animation(times3, x3, 4, "FHouse/FH-n.jump/FH-n.jump");
  Anim3.loop = false;
  Jumping = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);
  Jumping.resetAnim = true;

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  Jumping.addAllLists(1, 2, 0, -20);
  
  fDiaJump = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);
  fDiaJump.resetAnim = true;

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
      fDiaJump.addAllLists(1, 2, 11, -20);
  
  bDiaJump = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);
  bDiaJump.resetAnim = true;

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bDiaJump.addAllLists(1, 2, -7.5f, -20);
  
  Jumping2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  
  int[] ani4 = {1, 9};
  FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 10, 0);
    FDash.addAllLists(1, 5, 14, 0);
    
  int[] ani5 = {1, 10};
  BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, -8, 0);
  BDash.addAllLists(1, 5, -10, 0);

  
  
  int[] ani6 = {2, 3, 4, 5, 5};
  EditAction = new Action(ani6, 60, 8, 4, CurAction.LOW, true, true, true, false);
  EditAction.updFrameDataArr(0, 10);
  EditAction.updFrameDataArr_float(0, 0, 0);
  EditAction.addAllLists(1, 4, 2, -2);
  EditAction.addAllLists(2, 4, 2, -2);
  EditAction.HitBoxCollect.get(2).add(new ColCircle(120, -50, 200, 160, 3, -5, -1));
  EditAction.addAllLists(3, 15, 0, 0);
  
  int [] times7 = {4, 4, 4, 4, 3, 8, 11, 11};
  Animation Anim = new Animation(times7, 0, 8, "FHouse/FH-sp2-gr/FH-2sp-gr");
  Fireball = new FHaus_ProjAction(Anim, 20, 15, 12, 0, false, false, false, false, 1.99f, 80, 5.4f, 0, true, true, false, FB1AnimUp, FB1AnimMid, FB1AnimDown);
  Fireball.updFrameDataArr(0, 19);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball.addAllLists(1, 25, 0, 0);
  Fireball.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball.ProjAnim = FB1AnimMid;
  Fireball.destrEffAnim = BettDestrEff;
  
  
    //int[] ani7 = {2, 1, 1};
  Fireball2 = new FHaus_ProjAction(Anim, 20, 15, 12, 0, false, false, false, false, 1.8f, 80, 5.4f, -5, true, true, false, FB1AnimUp, FB1AnimMid, FB1AnimDown);
  Fireball2.updFrameDataArr(0, 19);
  Fireball2.updFrameDataArr_float(0, 0, 0);
  Fireball2.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball2.addAllLists(1, 25, 0, 0);
  Fireball2.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball2.ProjAnim = FB1AnimMid;
  Fireball2.destrEffAnim = BettDestrEff;
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action(new Animation(times2, 0, 4, "FHouse/FH-cr/HF-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-HHitSt"); LHit = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-LHitSt");
  HBlock = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-HBlockSt"); LBlock = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-LBlockSt");
  
  int[] ani9 = {6};
  Blocking = new Action(ani9);
  Blocking.updFrameDataArr(0, 2); 
  Blocking.updFrameDataArr_float(0, 0, 0);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 200, 160));
  
  //int[] ani10 = {2};
  //InHitStun = new Action(ani10, 0, 0, 0, 0, true, true, false, false);
  //InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160));
  
  //int[] ani11 = {11, 11};
  //Knockdown = new Action(ani11, 0, 0, 0, 0, true, true, false, false);

  
  int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD");
  //softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));

  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  
  int[] aniGrab = {6, 6, 7, 7};
  float[][] xyGrab = {{100, -80},
                      {120, -120},
                      {80, -100}, 
                    {140, -140}};
  GrabAction NormalGrab = new GrabAction(aniGrab, 0, 12, 4, -1, false, true, true, true, xyGrab); NormalGrab.hitCancel = false;
  NormalGrab.updFrameDataArr(0, 20); 
  NormalGrab.updFrameDataArr_float(0, 2, 0);
  NormalGrab.addAllLists(1, 20, 5, 0);
  NormalGrab.addAllLists(2, 20, 5, 0);
  NormalGrab.addAllLists(3, 2, 0, 0);
  
  int[] ani12 = {0, 7, 7};
  
      
  RoofNormal = new ChangeAction(ani12,  5, 2, 5, -1, true, true, false, false, NormalGrab); RoofNormal.hitCancel = false;
  RoofNormal.updFrameDataArr(0, 5); 
  RoofNormal.updFrameDataArr_float(0, 0, 0);
    RoofNormal.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  RoofNormal.addAllLists(1, 4, 3, 0);
  RoofNormal.HitBoxCollect.get(1).add(new ColCircle( 150, -125, 200, 160, 0, 0, -1));
  RoofNormal.HurtBoxCollect.get(1).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  RoofNormal.addAllLists(2, 3, 0, 0);
  RoofNormal.HurtBoxCollect.get(2).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  
  
    int x14 = 0;
    int[] times14 = {2, 2, 3, 2, 2};
  Animation Anim14 = new Animation(times14, x14, 5, "FHouse/FH-st.Light/FH-st.Light");
  LightNormal = new Action("FH-st-light", Anim14, 6, 6, 5, 3, true, false, false, false);

  
  int[] ani15 = {6, 7, 0, 0, 0};
  BaseNormal = new Action("MidNormal", ani15, 10, 5, 2, CurAction.HIGH, false, true, false, false);
  
    int x16 = 50;
    int[] times16 = {2, 2, 3, 2, 2, 5, 2, 1, 1, 2};
    Animation Anim16 = new Animation(times16, x16, 10,"FHouse/FH-st.Mid/FH-st.Mid");
  MidNormal2 = new Action("FH-st-med", Anim16,  12, 9, 15, 3, true, false, false, false);
  
    int x17 = 50;
    int[] times17 = {2, 2, 3, 2, 4, 6, 2, 1, 1, 2, 2, 2};
    Animation Anim17 = new Animation(times17, x17, 12,"FHouse/FH-st.Heavy/FH-st.Heavy");
  HeavyNormal = new Action("FH-st-heay", Anim17,  16, 12, 20, CurAction.MID, true, false, false, false);
  
  int[] times18 = {2, 2, 4, 2, 2};
  Animation Anim18 = new Animation(times18, 25, 5, "FHouse/FH-cr.Light/FH-cr.Light");
  cr_LightNormal = new Action("FH-cr-light", Anim18,  6, 5, 3, 3, true, false, false, false);

  
  int x19 = 50;
  int[] times19 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  Animation Anim19 = new Animation(times19, x19, 15, "FHouse/FH-cr.Mid/FH-cr.Mid");
  cr_MidNormal = new Action("FH-cr-med", Anim19,  60, 8, 4, CurAction.LOW, true, true, true, false);
 /*  cr_MidNormal.updFrameDataArr(0, 10);
  cr_MidNormal.updFrameDataArr_float(0, 0, 0);
  cr_MidNormal.addAllLists(1, 4, 2, -2);
  cr_MidNormal.addAllLists(2, 4, 2, -2);
  cr_MidNormal.HitBoxCollect.get(2).add(new ColCircle(120, -50, 200, 160, 3, -5, -1));
  cr_MidNormal.addAllLists(3, 15, 0, 0);*/
  
    int[] times23 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  cr_HeavyNormal = new Action( "FH-cr-heay", new Animation(times23, 50, 12, "FHouse/FH-cr.Heavy/HF-cr.Heavy"),
                               16, 8, 24, CurAction.MID, true, false, false, false);
  
    int x20 = 50;
  int[] times20 = {2, 2, 2, 2, 2};
  Animation Anim20 = new Animation(times20, x20, 50, 5, "FHouse/FH-j.Light/FH-j.Light");
  j_LightNormal = new Action("FH-j-light", Anim20, 5, 4, 3, CurAction.AIR, true, true, false, false);
  
  int[] times22 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  j_MidNormal = new extraForceOnHitAct( "FH-j-med", new Animation(times22, 0, 90, 10, "FHouse/FH-j.med/HF-j.med"),
                               12, 7, 14, CurAction.AIR, true, true, false, false, 3, -16);
  
  int[] times21 = {3, 4, 3, 4, 3, 2, 2};
  Animation Anim21 = new Animation( times21, 50, 50, 7, "FHouse/FH-j.Heavy/FH-j.Heavy");
  j_HeavyNormal = new Action("FH-j-heay", Anim21, 8, 5, 21, CurAction.AIR, true, true, false, false);
  j_HeavyNormal.updFrameDataArr_float(1, -6, -10);
  
  int[] times24 = {2, 2, 2, 2, 4, 2, 2, 2};
  Special1 = new Action("FH-sp1", new Animation( times24, 50, 8, "FHouse/FH-sp1/FH-1special"), 18, 16, 6, CurAction.MID, true, true, true, true);
   
   int[] times25 = {4, 3, 6, 2, 3, 6, 3, 3, 3};
   st_fHeavy = new Action("FH-st-forH",//"data/FH-st-fHeavy", 
   new Animation( times25, 75, 9, "FHouse/FH-st.fH/FH-st-fH"), 18, 14, 6, CurAction.HIGH, true, false, false, false);
   
    int[] times21p3 = {4, 4, 4, 4, 4};
    Action Special3p3 = new Action( //"HH-cr-Light",
    new Animation(times21p3, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-3p"), 6, 5, 4, 0, true, true, false, false);
    
    Action FillAct = Special3p3;
    FillAct.updFrameDataArr(0, 20);
  FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p2 = {2, 2, 2};
    Action Special3p2 = new ChangeOnCondAct( "FH-sp3p2",
    new Animation(times21p2, 25, 50, 3,"FHouse/FH-sp3/FH-3sp-2p"), 20, 10, 20, CurAction.HIGH, true, false, true, false, Special3p3);
    
         FillAct = Special3p2;
    FillAct.updFrameDataArr(0, 60);
  FillAct.updFrameDataArr_float(0, 16, 18);
   //FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p1 = {2, 2, 2, 2, 2};
    Special3 = new ChangeOnEndAct( //"HH-cr-Light",
    new Animation(times21p1, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-1p"), 6, 5, 4, CurAction.AIR, false, false, false, false, Special3p2); 
          FillAct = Special3;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
     int[] times26 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  Special4 = new Action("FH-sp4", new Animation( times26, 0, 14, "FHouse/FH-sp4/FH-4sp"), 10, 6, 10, CurAction.HIGH, true, true, true, true);
            FillAct = Special4;
    //FillAct.updFrameDataArr(0, 28);
    FillAct.updFrameDataArr_float(1, 14, -20);
    FillAct.updFrameDataArr_float(2, 4, 0);
    FillAct.updFrameDataArr_float(4, 4, 0);
   //FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
}
  public void st_Normals(){
        if(CurAction == fWalk){
      Force.x =  dirMult * sqrt(sq(sin(fWalk.AttAnim.timer* 0.1f))) * 6;
    }
    
        if(inputs[6] && firstPressInp[6]){
      changeAction(MidNormal2);
    }
    if(inputs[5] && firstPressInp[5]){
      changeAction(HeavyNormal);
    }
    if(inputs[4] && firstPressInp[4]){
      changeAction(LightNormal);
    }
    if(inputs[5] && firstPressInp[5] && ( (inputs[2] && dirMult == 1) || (inputs[3] && dirMult == -1) )  ){
      changeAction(st_fHeavy);
    }   
    
                        int[] DPmotion2 = {6, 3, 2, 3, 6}; 
    if( (compareBufferWithCombAtt(DPmotion) || compareBufferWithCombAtt(DPmotion2)) && inputs[5] && firstPressInp[5]){
           changeAction( Special1);  //RoofNormal);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
                          if(compareBufferWithCombAtt(FBmotion) && inputs[4] && firstPressInp[4]){
           changeAction( Fireball2);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
                          if(compareBufferWithCombAtt(FBmotion) && inputs[6] && firstPressInp[6]){
           changeAction( RoofNormal);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
  }
  
  public void cr_Normals(){
                    if(inputs[4] && firstPressInp[4]){
      changeAction(cr_LightNormal);
    } 
                if(inputs[6] && firstPressInp[6]){
      changeAction(cr_MidNormal);
    }     
         
               if(inputs[5] && firstPressInp[5]){
      changeAction(cr_HeavyNormal);
    }
  }
  
  public void j_Normals(){
             if(inputs[4] && firstPressInp[4]){
           changeAction(j_LightNormal);
         }
                  if(inputs[6] && firstPressInp[6]){
           changeAction(j_MidNormal);
         }
                  if(inputs[5] && firstPressInp[5]){
           changeAction(j_HeavyNormal);
         }
         
                  if(compareBufferWithCombAtt(FBmotion) && inputs[5] && firstPressInp[5]){
          //CurAction.reset();
          //CurAction = BaseNormal;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
    }   
         
         int[] revFBm = {2, 1, 4};
                  if(compareBufferWithCombAtt(revFBm) && inputs[4] && firstPressInp[4]){         
          changeAction(Special3);
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }  
         
                  if(compareBufferWithCombAtt(FBmotion) && inputs[4] && firstPressInp[4]){
          changeAction( Fireball);
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }
  }
  
  public void fighterActionsExtra(){
                            int[] DPmotion2 = {6, 3, 2, 3, 6}; 
    if( (compareBufferWithCombAtt(DPmotion) || compareBufferWithCombAtt(DPmotion2)) && inputs[5] && firstPressInp[5]){
           changeAction( Special1);  //RoofNormal);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         int[] revFB = {2, 1, 4}; 
    if( compareBufferWithCombAtt(revFB)  && inputs[5] && firstPressInp[5]){
           changeAction( Special4);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
  }
 
  
}

//HOCHHAUS###############################################################################################################################################################################################

class F_HHaus extends Fighter{
  Action RoofNormal;
  Action MidNormal;
  Action BaseNormal;
  Action Special1, Special2;
  ProjAction Fireball;
  Animation FB1Anim, FB1DestrEff;
  ProjAction Fireball2;
  ProjAction Fireball3;
  
  boolean hatOn = true;
  
  F_HHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {10, 10, 10, 10, 10, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 8, "HHouse/HH-st.idle/HH-st.idle");
      Standing = new Action("HH-st-idle", Anim0, false);
  }
  
  F_HHaus(int x, int y, char[] charinputs, boolean AI_Controlled){
    super(x, y, charinputs);
    this.AI_Controlled = AI_Controlled;
  }
  
   F_HHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
    F_HHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  

public void specSetup(){
  m = 0.75f;
  maxAirActions = 0;
  curAirActions = maxAirActions;
  CollisionBox = new ColRect(0, 0, 130, 200);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "HHouse/HHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  int[] etimes0 = {10, 10, 10, 10, 10};
  FB1Anim = new Animation(etimes0, 0, 0, 5, "Projectiles/Plane/HH-planeProjIdle");
  
  int[] etimes1 = {2, 2, 2, 2, 2, 2};
  FB1DestrEff = new Animation(etimes1, 0, 0, 6, "Effekte/PlaneDestrEff/HH-planeDestrEff");
  
  int[] times0 = {10, 10, 10, 10, 10, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 8, "HHouse/HH-st.idle/HH-st.idle");
      Standing = new Action("HH-st-idle", Anim0, false);
      
  int[] times2 = {5, 5, 5, 5, 5, 5, 5};
  Animation Anim1 = new Animation(times2, 30, 7,"HHouse/HH-fWalk/HF-fWalk");
      
  fWalk = new Action(//"HH-HeavyNormal",
  Anim1, 0, 0, 0, 0, true, false, false, false);
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 2.4f, 0);
  fWalk.resetAnim = false;
  
  bWalk = new Action(//"HH-HeavyNormal",
  Anim1, 0, 0, 0, 0, true, false, false, false);
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -1.6f, 0);
  bWalk.resetAnim = false;
  
  int[] times4 = {3, 2, 1, 4};
  Animation Anim4 = new Animation(times4, 0, 30, 4,"HHouse/HH-n.jump/HH-n.jump");
  
  Jumping = new Action(//"HH-HeavyNormal",
  Anim4, 0, 0, 0, 0, true, false, false, false);

      Jumping.updFrameDataArr(0, 10);
  Jumping.updFrameDataArr_float(0, 0, -18);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  
    fDiaJump = new Action(Anim4, 0, 0, 0, 0, true, false, false, false);

      fDiaJump.updFrameDataArr(0, 5);
  fDiaJump.updFrameDataArr_float(0, 8, -18);
  fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  
    bDiaJump = new Action(Anim4, 0, 0, 0, 0, true, false, false, false);

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, -6, -18);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  
  int[] ani4 = {1, 12, 1, 1};
  FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 10, 0);
    FDash.addAllLists(1, 5, 7, 0);
    
  int[] ani5 = {1, 11, 1, 1};
  BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, -5, 0);
  BDash.addAllLists(1, 5, -4, 0);
  
  
  int[] ani6 = {0, 5, 7, 4, 8};
  EditAction = new Action(ani6, 50, 15, 15, CurAction.LOW, true, true, true, false);
  EditAction.updFrameDataArr(0, 10);
  EditAction.updFrameDataArr_float(0, 0, 0);
  EditAction.addAllLists(1, 4, 2, 0);
  EditAction.addAllLists(2, 4, 2, 0);
  EditAction.HitBoxCollect.get(2).add(new ColCircle(120, -50, 300, 250, 3, -5, -1));
  EditAction.addAllLists(3, 15, 0, 0);
  
  int[] times7 = {3, 3, 3, 3, 4, 4, 4, 3, 2, 3, 3};
  Fireball = new HHaus_ProjAction1( new Animation(times7, 0, 11, "HHouse/HH-sp2/HH-2sp"),
  20, 15, 2, 0, false, false, false, false, 1, 300, -6.0f, 0, false, false, true);
  Fireball.ProjAnim = FB1Anim;
  Fireball.destrEffAnim = FB1DestrEff;
  Fireball.updFrameDataArr(0, 15);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  Fireball.addAllLists(1, 20, 0, 0);
  Fireball.HurtBoxCollect.get(1).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  
  int[] aniFB2 = {11, 7, 7};
    Fireball2 = new HHaus_ProjAction2(aniFB2, 20, 15, 2, 0, false, false, true, false, 1, -1, 4.0f, 0, false, false, false);
  Fireball2.updFrameDataArr(0, 25);
  Fireball2.updFrameDataArr_float(0, 0, 0);
  Fireball2.addAllLists(1, 40, 0, 0);
  
  int[] aniFB3 = {11, 3, 3};
    Fireball3 = new HHaus_ProjAction3(aniFB3, 0, 0, 0, 0, false, false, false, false, 1.2f, 200, 2.0f, 0, true, true, false);
  Fireball3.updFrameDataArr(0, 25);
  Fireball3.updFrameDataArr_float(0, 0, 0);
  Fireball3.addAllLists(1, 40, 0, 0);
  
    int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Crouching = new Action(new Animation(times1, 0, 7, "HHouse/HH-cr.idle/HH-cr.idl"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  
  //int[] times0_1 = {2,2};
  HHit = new Animation(times1, 0, 1, "HHouse/HH-Stun/HH-HHitSt"); LHit = new Animation(times1, 0, 1, "HHouse/HH-Stun/HH-LHitSt");
  HBlock = new Animation(times1, 0, 1, "HHouse/HH-Stun/HH-HBlockSt"); LBlock = new Animation(times1, 0, 1, "HHouse/HH-Stun/HH-LBlockSt");
  
  int[] ani9 = {3};
    Blocking = new Action(ani9);
  Blocking.updFrameDataArr(0, 2); 
  Blocking.updFrameDataArr_float(0, 0, 0);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 160, 300));
  
  //int[] ani10 = {11};
  //InHitStun = new Action(ani10);
  //InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 160, 300));
  
  //int[] ani11 = {10, 10};
  //Knockdown = new Action(ani11,  0, 0, 0, 0, true, true, false, false);
  
    int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"HHouse/HH-sKD/HH-air-sKD");
  
    int[] ani13 = {11};
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 160, 300, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
  int[] ani12 = {0, 9, 9};
  RoofNormal = new Action(ani12,  5, 2, 30, CurAction.HIGH, true, true, false, false);
  RoofNormal.updFrameDataArr(0, 15); 
  RoofNormal.updFrameDataArr_float(0, 0, 0);
  RoofNormal.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  RoofNormal.addAllLists(1, 4, 3, 0);
  RoofNormal.HitBoxCollect.get(1).add(new ColCircle( 150, -125, 160, 300, 0, 0, -1));
  RoofNormal.HurtBoxCollect.get(1).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  RoofNormal.addAllLists(2, 20, 0, 0);
  RoofNormal.HurtBoxCollect.get(2).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  
     int[] times15 = {2, 3, 1, 1, 2, 4};
  LightNormal = new Action("HH-st-light", new Animation(times15, 50, 6,"HHouse/HH-st.Light/HH-st.Light"),  5, 4, 2, 0, false, false, false, false);
  
   int[] times16 = {1, 1, 1, 1, 2, 2, 2, 3, 1, 1, 1, 1, 1, 1};
  MidNormal2 = new Action("HH-st-mid", new Animation(times16, 100, 14,"HHouse/HH-st.Mid/HH-st.Mid"),  12, 18, 30, 0, false, false, false, false);

  int[] times17 = {6, 6, 2, 2, 3, 2, 4, 7, 3, 3}; //35
  HeavyNormal = new Action("HH-HeavyNormal",
  new Animation(times17, 150, 10,"HHouse/HH-st.Heavy/HH-st.Heavy"), 20, 8, 28, 0, false, false, false, false);
  
    int[] times18 = {4, 5, 3, 2};
    cr_LightNormal = new Action( "HH-cr-Light",
    new Animation(times18, 25, 4,"HHouse/HH-cr.Light/HH-cr.Light"), 6, 5, 4, 0, false, false, false, false);
    
        int[] times19 = {3, 4, 4, 2, 4, 3, 2};
    cr_MidNormal = new Action( "HH-cr-med",
    new Animation(times19, 75, 7,"HHouse/HH-cr.med/HH-cr.med"), 20, 6, 12, CurAction.LOW, false, false, true, false);
    
        int[] times20 = {5, 5, 2, 2, 5, 3, 3};
    cr_HeavyNormal = new Action( "HH-cr-Heay",
    new Animation(times20, 25, 7,"HHouse/HH-cr.Heavy/HH-cr.Heavy"), 20, 15, 30, 0, false, false, false, false);
    
    
    int[] times21p3 = {2, 2, 2, 2};
    Action j_MidNormalp3 = new Action( //"HH-cr-Light",
    new Animation(times21p3, 30, 4,"HHouse/HH-j.med/p3/HF-j.med"), 6, 5, 4, 0, true, true, false, false);
    
    Action FillAct = j_MidNormalp3;
    FillAct.updFrameDataArr(0, 10);
  FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p2 = {2, 2};
    Action j_MidNormalp2 = new HoldButToKeepAct( "HH-j-medp2",
    new Animation(times21p2, 30, 2,"HHouse/HH-j.med/p2/HF-j.med"), 6, 5, 4, 0, false, false, false, true, j_MidNormalp3);
    
    int[] times21p1 = {2, 2, 2, 2, 2};
    j_MidNormal = new ChangeOnEndAct( //"HH-cr-Light",
    new Animation(times21p1, 30, 5,"HHouse/HH-j.med/p1/HF-j.med"), 6, 5, 4, 0, false, false, false, false, j_MidNormalp2); 
          FillAct = j_MidNormal;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
      int[] times22 = {3, 2, 4, 2};
    j_LightNormal = new Action( "HH-j-light",
    new Animation(times22, 30, 4,"HHouse/HH-j.light/FH-j.light"), 5, 3, 4, CurAction.AIR, true, true, false, false); 
    
          int[] times23 = {2, 2, 2, 2, 4, 2, 4, 2, 2, 2, 2};
    j_HeavyNormal = new Action( "HH-j-heay",
    new Animation(times23, 50, 30, 11,"HHouse/HH-j.Heavy/FH-j.Heavy"), 10, 7, 20, CurAction.HIGH, true, true, true, false); 
    j_HeavyNormal.gravMult = 0.93f;
   
            int[] times24 = {3, 3, 2, 6, 2, 2, 4, 4, 2, 2};
    Special1 = new Action( "HH-sp1",
    new Animation(times24, 30, 10,"HHouse/HH-sp1/HH-1sp"), 15, 7, 12, CurAction.MID, true, true, false, true); 
    FillAct = Special1;
    FillAct.updFrameDataArr_float(1, 16, -1);
    //FillAct.updFrameDataArr_float(2, 12, -1);
    
      int[] times25 = {24, 5, 5, 5, 16, 8, 8};
  float[][] xyGrab = {{0, -280},
                      {-180, -120},
                      {-180, 0}, 
                    {0, 100}};
  GrabAction NormalGrab = new GrabAction(new Animation(times25, 0, 7,"HHouse/HH-sp3/HH-3sp-2p"), 30, 12, 11, -1, false, false, true, true, xyGrab);
  NormalGrab.updFrameDataArr(0, 20); 
  NormalGrab.updFrameDataArr_float(0, 0, 0);
  NormalGrab.addAllLists(1, 20, 0, -2);
  NormalGrab.addAllLists(2, 20, 0, -2);
  NormalGrab.addAllLists(3, 2, 0, -20);
  
      int[] times26 = {2, 2, 2, 3, 6, 6, 6, 3, 3};
  Special2 = new ChangeAction("HH-sp3-p1", new Animation(times26, 0, 9,"HHouse/HH-sp3/HH-3sp-1p"),
  5, 2, 5, -1, true, true, false, false, NormalGrab);
 /* Special2 .updFrameDataArr(0, 6); 
  
    Special2 .HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  Special2 .addAllLists(1, 12, 3, 0);
  
  Special2 .HurtBoxCollect.get(1).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  Special2 .addAllLists(2, 10, 0, 0);
  Special2 .HurtBoxCollect.get(2).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));*/
    Special2 .HitBoxCollect.get(1).add(new ColCircle( 10, -270, 280, 200, 0, 0, -1));
    Special2 .updFrameDataArr_float(0, 10, -20);
}

public void dash(){
      if(compareBufferWithCombAtt(bDashmotion) && (y >= GROUNDHEIGHT || CollisionBox.bottom)
      ){
          CurAction.reset();
          CurAction = BDash;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }
}

public boolean ChargeDirCheck(int chargeDur, int dir){
  
  return (inputChargeT[2] >= chargeDur && dirMult * dir == 1) || (inputChargeT[3] >= chargeDur && dirMult * dir == -1);
}

public void st_Normals(){
      if(inputs[4] && firstPressInp[4]){
      changeAction(LightNormal);
    }
    if(inputs[5] && firstPressInp[5]){
      changeAction(HeavyNormal);
    }
    if(inputs[6] && firstPressInp[6]){
      changeAction(MidNormal2);
    }
    
               int[] backCharge = {6}; 
              if( ChargeDirCheck(20, -1) && inputs[4] && firstPressInp[4] && compareBufferWithCombAtt(backCharge) ){
          changeAction(Special1);

    }
     
    
             if(compareBufferWithCombAtt(FBmotion) && inputs[4] && firstPressInp[4]){
          CurAction.reset();
          CurAction = Fireball;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }   
    
        if(compareBufferWithCombAtt(FBmotion) && inputs[6] && firstPressInp[6]){
          CurAction.reset();
          CurAction = Fireball3;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }    
    
    int[] DPmotion2 = {6, 3, 2, 3, 6}; 
    if((compareBufferWithCombAtt(DPmotion) || compareBufferWithCombAtt(DPmotion2)) && inputs[5] && firstPressInp[5] && hatOn){
          CurAction.reset();
          CurAction = Fireball2;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
          hatOn = false;
    }
    int[] revFB = {2, 1, 4}; 
        if(compareBufferWithCombAtt(revFB) && inputs[4] && firstPressInp[4]){
          CurAction.reset();
          CurAction = Special2;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }
    
}

public void cr_Normals(){
    if(inputs[4] && firstPressInp[4]){
      changeAction(cr_LightNormal);//EditAction;
    }
    if(inputs[5] && firstPressInp[5]){
      changeAction(cr_HeavyNormal);
    }
    if(inputs[6] && firstPressInp[6]){
      changeAction(cr_MidNormal);
    }
    
                   int[] backCharge = {6}; 
              if( ChargeDirCheck(20, -1) && inputs[4] && firstPressInp[4] && compareBufferWithCombAtt(backCharge) ){
          changeAction(Special1);
    }
    
}

public void j_Normals(){
    if(inputs[6] && firstPressInp[6]){
      changeAction(j_MidNormal);
    }
    
    if(inputs[4] && firstPressInp[4]){
      changeAction(j_LightNormal);
    }
    
        if(inputs[5] && firstPressInp[5]){
      changeAction(j_HeavyNormal);
    }

}


public void extraStuff(){
  for(int i = Projectiles.size()-1; i >= 0; i--){
    if(Projectiles.get(i) instanceof BoomerangProj){
        if(Projectiles.get(i).x >= CollisionBox.x - CollisionBox.br/2 &&  Projectiles.get(i).x <= CollisionBox.x + CollisionBox.br/2){
          Projectiles.remove(i);
          hatOn = true;
  }
 
    }   
     else{hatOn = true;}
  }
}



}


//Wohnwagen###############################################################################################################################################################################################
class F_WHaus extends Fighter{
  
  boolean stance = false;

  Action Stance, stanceFWalk, stanceBWalk;
  Action StanceLight, StanceMed, StanceHeavy;

  Action st_fHeavy;
  ProjAction Fireball;
  ProjAction Fireball2;
  Action Special1, Special2, Special3 ;
  
  F_WHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "WHouse/WH-st.idle/WH-st");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_WHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
    F_WHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
public void setup(){
  maxAirActions = 1;
  curAirActions = maxAirActions;
   CollisionBox = new ColRect(0, 0, 120, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "WHouse/WH-st.idle/WH-st");
      Standing = new Action("FH-st-idle", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
  
      
  int x1 = 0;
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times0, x1, 1, "WHouse/WH-fWalk/WH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 3.2f, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2.2f, 0);
  
  Animation Anim1_1 = new Animation(times0, 0, 1, "WHouse/WH-stance/WH-stance");
      Stance = new Action( Anim1_1, false);
      Stance.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      Stance.updFrameDataArr(0, 1);
     Stance.updFrameDataArr_float(0, 0, 0);

  Animation Anim2_1 = new Animation(times0, x1, 1, "WHouse/WH-stance-fWalk/WH-stance-fW");
  stanceFWalk = new Action(Anim2_1, false);//0, 0, 0, 0, true, false, false, false);
  stanceFWalk.addingForce = true;
  stanceFWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  stanceFWalk.updFrameDataArr(0, 1);
  stanceFWalk.updFrameDataArr_float(0, 1.6f, 0);//3, 0);
  
  Animation Anim2_2 = new Animation(times0, x1, 1, "WHouse/WH-stance-bWalk/WH-stance-bW");
  stanceBWalk = new Action(Anim2_2, false);
  stanceBWalk.addingForce = true;
  stanceBWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  stanceBWalk.updFrameDataArr(0, 1);
  stanceBWalk.updFrameDataArr_float(0, -1.2f, 0);
  
  int[] times3 = {2, 3};
  Animation Anim3 = new Animation(times3, 0, 2, "WHouse/WH-nJump/WH-nJump");
  Anim3.loop = false;
  Jumping = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.addAllLists(1, 2, 0, -18);
  
  fDiaJump = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.addAllLists(1, 2, 10, -15);
  
  bDiaJump = new Action(Anim3, 0, 0, 0, 0, true, true, false, false);

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.addAllLists(1, 2, -7.5f, -15);
  
    Jumping2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action(Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  int[] ani4 = {1, 9};
  FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 10, 0);
    FDash.addAllLists(1, 5, 10, 0);
    
  int[] ani5 = {1, 10};
  BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, -8, 0);
  BDash.addAllLists(1, 5, -8, 0);

  
  
  int[] ani6 = {2, 3, 4, 5, 5};
  EditAction = new Action(ani6, 60, 8, 4, CurAction.LOW, true, true, true, false);
  EditAction.updFrameDataArr(0, 10);
  EditAction.updFrameDataArr_float(0, 0, 0);
  EditAction.addAllLists(1, 4, 2, -2);
  EditAction.addAllLists(2, 4, 2, -2);
  EditAction.HitBoxCollect.get(2).add(new ColCircle(120, -50, 200, 160, 3, -5, -1));
  EditAction.addAllLists(3, 15, 0, 0);
  
  int[] ani7 = {2, 1, 1};
  Fireball = new ProjAction(ani7, 20, 15, 12, 0, false, false, false, false, 1.6f, 200, 4.0f, 0, true, true, false);
  Fireball.updFrameDataArr(0, 20);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.addAllLists(1, 25, 0, 0);
  
    //int[] ani7 = {2, 1, 1};
  Fireball2 = new ProjAction(ani7, 20, 15, 12, 0, false, false, false, false, 1.6f, 200, 4.0f, -4, true, true, false);
  Fireball2.updFrameDataArr(0, 25);
  Fireball2.updFrameDataArr_float(0, 0, 0);
  Fireball2.addAllLists(1, 30, 0, 0);
  
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action(new Animation(times2, 0, 1, "WHouse/WH-cr/WH-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-HHitSt"); LHit = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-LHitSt");
  HBlock = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-HBlockSt"); LBlock = new Animation(times1, 0, 1, "FHouse/FH-Stun/FH-LBlockSt");
  
  int[] ani9 = {6};
  Blocking = new Action(ani9);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 200, 160));
  
  int[] ani10 = {2};
  InHitStun = new Action(ani10, 0, 0, 0, 0, true, true, false, false);
  InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160));
  
  int[] ani11 = {11, 11};
  Knockdown = new Action(ani11, 0, 0, 0, 0, true, true, false, false);
  Knockdown.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.updFrameDataArr(0, 1); 
  Knockdown.updFrameDataArr_float(0, 0, 0);
  Knockdown.addAllLists(1, 40, 0, 0);
  
    int[] times0_0 = {2,2}; 
  softKD = new Action(new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD"),
  0, 0, 0, 0, true, true, false, false);
  softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  softKD.updFrameDataArr(0, 1); 
  softKD.updFrameDataArr_float(0, 0, 0);
  softKD.addAllLists(1, 40, 0, 0);
  
  int[] aniGrab = {6, 6, 7, 7};
  float[][] xyGrab = {{100, -80},
                      {120, -120},
                      {80, -100}, 
                    {140, -140}};
  GrabAction NormalGrab = new GrabAction(aniGrab, 0, 12, 0, 0, false, true, true, false, xyGrab);
  NormalGrab.updFrameDataArr(0, 20); 
  NormalGrab.updFrameDataArr_float(0, 2, 0);
  NormalGrab.addAllLists(1, 20, 5, 0);
  NormalGrab.addAllLists(2, 20, 5, 0);
  NormalGrab.addAllLists(3, 2, 0, 0);
     
  
  int[] ani13 = {11};
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 200, 160, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
    int[] times14 = {4, 2, 4, 4};
  Animation Anim14 = new Animation(times14, 0, 4, "WHouse/WH-st-light/WH-st-light");
  LightNormal = new Action("WH-st-light", Anim14, 8, 6, 5, 3, true, true, false, false);
  
    int[] times16 = {2, 4, 4, 3, 2, 5, 3, 2, 2, 2};
    Animation Anim16 = new Animation(times16, 70, 10,"WHouse/WH-st-med/WH-st-med");
  MidNormal2 = new Action("WH-st-med", Anim16,  16, 10, 15, 3, true, true, false, false);
  
    int[] times17 = {4, 4, 4, 2, 6, 6, 4, 2};
    Animation Anim17 = new Animation(times17, 70, 8,"WHouse/WH-st-heay/WH-st-heay");
  HeavyNormal = new Action("WH-st-heay", Anim17,  18, 12, 20, CurAction.MID, true, true, false, false);
  
  int[] times18 = {5, 6, 4};
  Animation Anim18 = new Animation(times18, 0, 3, "WHouse/WH-cr-light/WH-cr-light");
  cr_LightNormal = new Action("WH-cr-light", Anim18,  6, 5, 3, 3, true, false, false, false);

  int[] times19 = {4, 4, 2, 2, 4, 4, 2, 2, 2, 4, 4};
  Animation Anim19 = new Animation(times19, 70, 11, "WHouse/WH-cr-med/WH-cr-med");
  cr_MidNormal = new Action("Wh-cr-med", Anim19,  60, 8, 4, CurAction.LOW, true, true, true, false);
 /*  cr_MidNormal.updFrameDataArr(0, 10);
  cr_MidNormal.updFrameDataArr_float(0, 0, 0);
  cr_MidNormal.addAllLists(1, 4, 2, -2);
  cr_MidNormal.addAllLists(2, 4, 2, -2);
  cr_MidNormal.HitBoxCollect.get(2).add(new ColCircle(120, -50, 200, 160, 3, -5, -1));
  cr_MidNormal.addAllLists(3, 15, 0, 0);*/
  
    int[] times23 = {6, 3, 3, 6, 4, 4, 2, 6};
  cr_HeavyNormal = new Action( "Wh-cr-heay", new Animation(times23, 0, 8, "WHouse/WH-cr-heay/WH-cr-heay"),
                               16, 8, 40, CurAction.MID, true, true, true, false);
  
  int[] times20 = {3, 2, 4, 3};
  Animation Anim20 = new Animation(times20, 30, 0, 4, "WHouse/WH-j-light/WH-j-light");
  j_LightNormal = new Action("Wh-j-light", Anim20, 5, 4, 3, CurAction.AIR, true, true, false, false);
  
  int[] times22 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  j_MidNormal = new extraForceOnHitAct( "FH-j-med", new Animation(times22, 0, 90, 10, "FHouse/FH-j.med/HF-j.med"),
                               12, 7, 14, CurAction.AIR, true, true, false, false, 3, -16);
  
  int[] times21 = {3, 3, 3, 4, 3, 4, 2, 2, 2, 4, 4, 2};
  Animation Anim21 = new Animation( times21, 50, 0, 12, "WHouse/WH-j-heay/WH-j-heay");
  j_HeavyNormal = new Action("Wh-j-heay", Anim21, 8, 5, 6, CurAction.AIR, true, true, false, true);
  j_HeavyNormal.gravMult = 0.90f;
           Action FillAct = j_HeavyNormal;
   FillAct.updFrameDataArr_float(0, 14, 0);
  
  int[] times24 = {2, 2, 2, 2, 4, 2, 2, 2};
  Special1 = new Action("FH-sp1", new Animation( times24, 50, 8, "FHouse/FH-sp1/FH-1special"), 18, 16, 8, CurAction.MID, true, true, true, true);
   
   int[] times25 = {4, 3, 6, 2, 3, 6, 3, 3, 3};
   st_fHeavy = new Action("FH-st-forH",//"data/FH-st-fHeavy", 
   new Animation( times25, 75, 9, "FHouse/FH-st.fH/FH-st-fH"), 18, 14, 20, CurAction.HIGH, true, false, false, false);
   
            int[] times27 = { 3, 6, 4, 2, 2, 3, 2, 2};
   Action Special2p2 = new Action(//"FH-st-forH",
      new Animation( times27, 0, 200, 8, "WHouse/WH-sp1/WH-1sp2p"), 18, 8, 18, CurAction.HIGH, true, false, false, false);
      Special2p2.hitCancel = false;
      FillAct = Special2p2;
   FillAct.updFrameDataArr(0, 15);
   FillAct.updFrameDataArr_float(0, 10, -20);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, 0, 330, 100));
      FillAct.HitBoxCollect.get(0).add(new ColCircle(30, 0, 360, 120));
   FillAct.addAllLists(1, 7, 0, 0);
      FillAct.HurtBoxCollect.get(1).add(new ColCircle(0, 0, 330, 100));
   FillAct.HitBoxCollect.get(1).add(new ColCircle(30, 0, 360, 120));
   FillAct.addAllLists(2, 2, 0, 0);
   
      int[] times26 = {3, 3, 3, 2, 2};
   Special2 = new ChangeOnEndAct(//"FH-st-forH",
   new Animation( times26, 0, 5, "WHouse/WH-sp1/WH-1sp"), 18, 6, 10, CurAction.HIGH, true, true, false, false, Special2p2);
   Special2.hitCancel = false;
   FillAct = Special2;
   FillAct.updFrameDataArr(0, 6);
   FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   FillAct.addAllLists(1, 4, 6, 0);
   FillAct.addAllLists(2, 1, 24, -42);
   
         int[] times28 = {3, 3, 2, 2, 2, 4, 2, 2, 2, 2};
   Special3 = new Action("Wh-sp2",
   new Animation( times28, 100, 10, "WHouse/WH-sp2/WH-2sp"), 32, 10, 4, CurAction.MID, true, true, false, false);
   Special3.hitCancel = false;
  
    int[] etimes1 = {2, 4, 3, 3};
  StanceLight = new Action("WH-stance-l", 
  new Animation(etimes1, 0, 4, "WHouse/WH-stance-light/WH-stance-l"), 6, 4, 5, CurAction.LOW, true, false, false, false);
  
    int[] etimes2 = {3, 3, 3};
  StanceMed = new Action("WH-stance-m", 
  new Animation(etimes2, 0, 3,"WHouse/WH-stance-Med/WH-stance-m"),  4, 1, 10, CurAction.MID, true, true, false, false);
  
    int[] etimes3 = {3, 3, 3, 3, 6};
  StanceHeavy = new Action("WH-stance-heay", 
  new Animation(etimes3, 0, 5,"WHouse/WH-stance-heavy/WH-stance-h"),  14, 12, 20, CurAction.HIGH, true, true, false, true);
}

  public void standingStateReturn(Fighter Opp){
    if(stance){
      CollisionBox.ho = 100;
      CurAction.playAction2(this, Opp, Stance);
    }
    else if(!stance){
              if((y == GROUNDHEIGHT || CollisionBox.bottom) && inputs[1]){
        CurAction.playAction2(this, Opp, Crouching);
      }
      else if((y >= GROUNDHEIGHT || CollisionBox.bottom) && CurAction.attKind == 4){
        changeAction(Crouching);
      }
      else{
        CollisionBox.ho = 180;
        CurAction.playAction2(this, Opp, Standing);
      }
    }
    
      if(y >= GROUNDHEIGHT || CollisionBox.bottom){
        curAirActions = maxAirActions;
      }
          if(CurAction == InHitStun || CurAction == softKD || CurAction == Knockdown){
      stance = false;
    }

  }
  
  public void fighterActionsExtra(){

    
    if( (stance && cancelWindow > 0) || CurAction == Stance || CurAction == stanceBWalk || CurAction == stanceFWalk){
            if(inputs[2]){
      if(facing == RIGHT){
        CurAction.reset();
        CurAction = stanceFWalk;
        curSuper++;
         }
      else{
        CurAction.reset();
        CurAction = stanceBWalk;
        curSuper++;
         }    
    }
    
        if(inputs[3]){
       if(facing == LEFT){
     
        CurAction.reset();
        CurAction = stanceFWalk;
        curSuper++;
         }
         else{
        CurAction.reset();
        CurAction = stanceBWalk;
        curSuper++;
         }
        }
        
    
        if(inputs[4] && firstPressInp[4]){
      changeAction(StanceLight);
    } 
        if(inputs[6] && firstPressInp[6]){
      changeAction(StanceMed);
      Force.x *= -1;
      dirMult *= -1;
      if(facing == RIGHT){
        facing = LEFT;
      }
      else if(facing == LEFT){
        facing = RIGHT;
      }
    } 
    if(inputs[5] && firstPressInp[5]){
      changeAction(StanceHeavy);
    } 
      
         int[] Stmotion = {2, 2}; 
    if( compareBufferWithCombAtt(Stmotion)){
      stance = false;
      //changeAction( Standing);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
                     if(inputs[5] && firstPressInp[5] && (Force.x >= 5 || Force.x <= -5) && ( (inputs[2] && dirMult == 1) || (inputs[3] && dirMult == -1) )){
      changeAction(Special2);
    }
       
    }
       
  }
  
 

  public void st_Normals(){
    
        if(inputs[6] && firstPressInp[6]){
      changeAction(MidNormal2);
    }
                                     int[] HalfCircle = {4, 1, 2, 3, 6}; 
    if(// compareBufferWithCombAtt(HalfCircle) && 
    inputs[6] && firstPressInp[6]  && dirMult*Force.x <= -2){
           changeAction( Special3);  //RoofNormal);
         }
    
    if(inputs[5] && firstPressInp[5]){
      changeAction(HeavyNormal);
    }
        if(inputs[5] && firstPressInp[5] && (Force.x >= 5 || Force.x <= -5)){
          stance = true;
      changeAction(Special2);    
    }
    
    if(inputs[4] && firstPressInp[4]){
      changeAction(LightNormal);
    }
    
                        int[] DPmotion2 = {6, 3, 2, 3, 6}; 
    if( (compareBufferWithCombAtt(DPmotion) || compareBufferWithCombAtt(DPmotion2)) && inputs[5] && firstPressInp[5]){
           changeAction( Special1);  //RoofNormal);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
                          if(compareBufferWithCombAtt(FBmotion) && inputs[4] && firstPressInp[4]){
           changeAction( Fireball2);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
  }
  
  public void cr_Normals(){
                    if(inputs[4] && firstPressInp[4]){
      changeAction(cr_LightNormal);
    } 
                if(inputs[6] && firstPressInp[6]){
      changeAction(cr_MidNormal);
    }     
         
               if(inputs[5] && firstPressInp[5]){
      changeAction(cr_HeavyNormal);
    }
  }
  
  public void j_Normals(){
             if(inputs[4] && firstPressInp[4]){
           changeAction(j_LightNormal);
         }
                  if(inputs[6] && firstPressInp[6]){
           changeAction(j_MidNormal);
         }
                  if(inputs[5] && firstPressInp[5]){
           changeAction(j_HeavyNormal);
         }
           
         
                  if(compareBufferWithCombAtt(FBmotion) && inputs[4] && firstPressInp[4]){
          CurAction.reset();
          CurAction = Fireball;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }
  }
 
  
}
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
  float gravMult = 1.f;
  
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
  
  public void playAction2(Fighter Pl, Fighter Opp, Action ToSetTo){
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
  
  public void clearAndCurBoxes(Fighter Pl){
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
  
  public void changeXDirOfBoxes(){
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
  
  public void changeXDirOfForces(){
    for(int i = 0; i < setForceAtDur.length; i++){
      setForceAtDur[i][0] *= -1;
    }
  }
  
  
  
  public void reset(){
      curMoveDur = 0;
      curCollumn = 0;
      firstHit = true;
  }
  
  
  public void updFrameDataArr(int index, int value){
          int[] l_frameData = new int[HitBoxCollect.size()];
      for(int i = 0; i < whenToUpdBoxs.length; i++){
        l_frameData[i] = whenToUpdBoxs[i];
      }
      whenToUpdBoxs = l_frameData;
      
      whenToUpdBoxs[index] = value;
  }
  
    public void updFrameDataArr_float(int index, float fx, float fy){
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
  
  public int sumOfFrameArr(int index){
    int sum = 0;
    for(int i = 0 ; i < index; i++){
      sum += whenToUpdBoxs[i];
    }
    
    return sum;
  }
  
  public void copy(Action a){
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
  
  public void addAllLists(int index, int frameData, float fx, float fy){
         HitBoxCollect.add(new ArrayList<ColCircle>());
    HurtBoxCollect.add(new ArrayList<ColCircle>());
      updFrameDataArr(index, frameData);
    updFrameDataArr_float(index, fx, fy);
  }
  
  public void alwaysSpecialEffect(Fighter Pl, Fighter Opp){
  }
  
  public void specialEffect(Fighter Pl, Fighter Opp){
  }
  
  public void specialEffectOnHit(Fighter Pl, Fighter Opp){
  }
  
  public void specialEffectOnEnd(Fighter Pl, Fighter Opp){
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
  
    public void specialEffectOnHit(Fighter Pl, Fighter Opp){
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
  
    public void specialEffectOnEnd(Fighter Pl, Fighter Opp){
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
  
    public void alwaysSpecialEffect(Fighter Pl, Fighter Opp){
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
  
     public void alwaysSpecialEffect(Fighter Pl, Fighter Opp){
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
  
     public void specialEffectOnEnd(Fighter Pl, Fighter Opp){
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
  
    public void specialEffectOnHit(Fighter Pl, Fighter Opp){
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
  
  public void specialEffect(Fighter Pl, Fighter Opp){
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
  
  public void specialEffectOnHit(Fighter Pl, Fighter Opp){
    //Opp.CurAction.reset();
    Opp.CurAction = Opp.BeingGrapped;
       //Ok, Ich bin dumm, also echt, nur um den Damage auszugleichen // funktioniert eh nicht, too BAD
  
  }
  
  public void specialEffectOnEnd(Fighter Pl, Fighter Opp){
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
  
  public void specialEffect(Fighter Pl, Fighter Opp){
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
  
    public void specialEffect(Fighter Pl, Fighter Opp){
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
 
    public void specialEffect(Fighter Pl, Fighter Opp){
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
 
    public void specialEffect(Fighter Pl, Fighter Opp){
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
  
      public void specialEffect(Fighter Pl, Fighter Opp){
    if(curCollumn == 1){
    Pl.Projectiles.add(new MatteProj(Pl.x + 200 * Pl.dirMult, Pl.y-180, 0, 0, Pl.dirMult * fx , fy, m, exTimer, affHitStunT, affBlockStunT, damage, effByFric, effByGrav, destroyedByCol, 300, 100));
  } 
  }
  
}
class ColBox{
  
  public void draw(){
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
  
  public void draw(int dir){
    rect(x - br/2 + addx*dir, y - ho/2 + addy, br, ho);
    line(x + addx*dir, y +addy, x + (addx + forcex*5)*dir, y +addy + forcey*5);
  }
  
  public void setxy(float x2, float y2){
     this.x = x2;
     this.y = y2;
  }
  
  public boolean compare(ColCircle c, int dir, int dir2){
    
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
  
  public void draw(){
    rect(x + addx, y + addy - ho, br, ho);
  }
  
    public void colCheckRect( Fighter Pl, Fighter Opp){
      ColRect Pc = Pl.CollisionBox;
      ColRect other = Opp.CollisionBox;
    
    if(Pc.y + Pl.Force.y >= other.y - other.ho && Pc.y + Pl.Force.y <= other.y 
     && (( Pc.x + Pc.br >= other.x && Pc.x <= other.x ) || ( Pc.x + Pc.br >= other.x + other.br && Pc.x <= other.x + other.br) || (Pc.x >= other.x && Pc.x + Pc.br <= other.x + other.br))
    ){
      //if(Pl.y == Opp.y - Opp.CollisionBox.ho){
        bottom = true;
      //}
    }
    else{
      bottom = false;
    }
    
     if(Pc.y - Pc.ho + Pl.Force.y >= other.y && Pc.y - Pc.ho + Pl.Force.y <= other.y -other.ho
     && (( Pc.x + Pc.br >= other.x && Pc.x <= other.x ) || ( Pc.x + Pc.br >= other.x + other.br && Pc.x <= other.x + other.br) || (Pc.x >= other.x && Pc.x + Pc.br <= other.x + other.br))
     ){

      top = true;
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

  public boolean compare(ColRect c){
    
    return recRecColCheck(x - br/2 + addx, y- ho/2 + addy, br, ho, c.x - c.br/2 + c.addx, c.y - c.ho/2 + c.addy, c.br, c.ho);
  }
  
 public boolean ColRectLineVerCheck(int x){
   if(this.x - this.br/2 <= x && x <= this.x + this.br/2){
     return true;
   }
   return false;
 }
  
}
class Condition{
  Condition(){}
  
  public boolean cond(Fighter Pl, Fighter Opp){
    return true;
  }
}

class dirCombCond extends Condition{
  int[] motion = {5};
  dirCombCond(int[] motion){this.motion = motion;}
  public boolean cond(Fighter Pl, Fighter Opp){
    return Pl.compareBufferWithCombAtt(motion);
  }
}

class ButCond extends Condition{
  int ButIndex = 0;
  ButCond(int ButIndex){ this.ButIndex = ButIndex;}
  public boolean cond(Fighter Pl, Fighter Opp){
    return Pl.inputs[ButIndex];
  }
}

class fPButCond extends ButCond{
  fPButCond(int ButIndex){ super(ButIndex);}
  public boolean cond(Fighter Pl, Fighter Opp){
    return Pl.inputs[ButIndex] && Pl.firstPressInp[ButIndex];
  }
}

class dirHorFPButC extends ButCond{ // for dir CommandNormals
  int holdBut = 0;
  dirHorFPButC(int ButIndex, int holdBut){ super(ButIndex); this.holdBut = holdBut;}
  public boolean cond(Fighter Pl, Fighter Opp){
    return Pl.inputs[ButIndex] && Pl.firstPressInp[ButIndex] && Pl.inputs[holdBut];
  }
}

class comfPButC extends ButCond{
  int[] motion = {5};
  comfPButC(int ButIndex, int[] motion){ super(ButIndex); this.motion = motion;}
  public boolean cond(Fighter Pl, Fighter Opp){
    return Pl.compareBufferWithCombAtt(motion) && Pl.inputs[ButIndex] && Pl.firstPressInp[ButIndex];
  }
}

class MenuBox{
  float x;
  float y;
  float breite;
  float hoehe;
  String boxText = "";
  int farbe = 255;
  boolean clicked = false;
  
public MenuBox(float xmb, float ymb, float bmb, float hmb){
   this.x = xmb;
   this.y = ymb;
   this.breite = bmb;
   this.hoehe = hmb;
}
  
public MenuBox(float xmb, float ymb, float bmb, float hmb, String boxTxtmb, int f){
  this(xmb, ymb, bmb, hmb);
   this.boxText = boxTxtmb;
   this.farbe = f;
 }

public void draw(){
  menuBoxClick();
  displayMenuBox();
}
 
  
public void displayMenuBox(){
  
  rectMode(CORNER);
  stroke(0);
  fill(this.farbe);
  rect(x,y, breite, hoehe);
  textSize(40);
  textAlign(CENTER);
  fill(0);
  text(this.boxText,x+breite/2,y+hoehe/2);  
  
  if(x <= mouseX && mouseX <= x + breite && y <=mouseY &&mouseY <= y + hoehe){
    blink(x + breite, y +hoehe/2);
  }
} 

public void menuBoxClick(){
  if(click && x <= mouseX && mouseX <= x + breite && y <=mouseY &&mouseY <= y + hoehe){
    clicked = true;
  }else clicked = false;
}

public void blink(float xb, float yb){
  float xC;
  float yC;
 
    yC = yb;
    xC = xb - (millis() % 10);
    
     triangle(xC,yC,xC+20,yC-10,xC+20,yC+10);
}

public void umrandung(){
    rectMode(CORNER);
    fill(0);
    rect(x-breite/10,y-hoehe/10, breite +breite/10*2, hoehe +hoehe/10*2); 
}

}




class EintragBox extends MenuBox{
  int count = 0;
  int maxCount;
  
  EintragBox(float xmb, float ymb, float bmb, float hmb, int maxCount){
    super(xmb, ymb, bmb, hmb);
    this.maxCount = 30;
  }
  
  public void draw(){
    eintragClick();
    displayMenuBox();
  }
  
  public void eintragClick(){
    if(mousePressed && mouseX > x && mouseX < x + breite && mouseY > y && mouseY < y+hoehe){
    clicked = true;
  }else if(!(mouseX > x && mouseX < x + breite && mouseY > y && mouseY < y+hoehe)){
    clicked = false;
  }
  if(clicked){
    umrandung();
  }
  }
  
  public void keyReleased(){
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
class Stage{
  String datnam = "none";
  int plLine = 0, stageDeepness = 0;
  PImage[] hinterSchichten, vorSchichten;
  
  Stage(String datnam, int stageDeepness, int plLine){
    this.datnam = datnam;
    this.stageDeepness = stageDeepness;
    this.plLine = plLine;
    hinterSchichten = new PImage[plLine];
    vorSchichten = new PImage[stageDeepness-plLine];
    loadImages();
  }
  
  public void loadImages(){
    for(int i = 0; i < plLine; i++){
      hinterSchichten[i] = loadImage("Stages/" + datnam + i + ".png");
    }
    for(int i = 0; i < stageDeepness-plLine; i++){
      vorSchichten[i] = loadImage("Stages/" + datnam + (plLine+ i) + ".png");
    }
  }
  
  public void drawBackground(){
    imageMode(CENTER);
    for(int i = 0; i < 1//plLine
    ; i++){
      PImage p = hinterSchichten[i];
      image(p, p.width/4 - (Camerabox.x-initWidth/4)*0.05f*i, 0);
    }
  }
  
  public void drawForeground(){
        imageMode(CENTER);
    for(int i = 0; i < stageDeepness-plLine; i++){
      PImage p = vorSchichten[i];
      image(p, p.width/4, 0);
    }
  }
  
  
}
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
  
  public void handleAnim(){
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
  
  public void Reset(){
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
  
  float m = 1.2f;
  
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
  
 public void specialStuff(Fighter Pl, Fighter Opp){
 }
 
 public void specialOnHit(Fighter Pl, Fighter Opp){
 }
  
  public void gameLogic(Fighter Pl, Fighter Opp){
    if(Anim != null){
      Anim.handleAnim();
    }
    if(effByGrav){
      forcey += m * 0.1f;
    }
    if(effByFric){
      if(forcex < 0){
        forcex += 0.02f;
      }
      else if(forcex > 0){
        forcex -= 0.02f;
      }
    }
    if(!(-0.05f < forcex && forcex < 0.05f)){  
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
  
  public void draw(){
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
  
    public void draw(){
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
  
  public void specialStuff(Fighter Pl, Fighter Opp){
    if(y < GROUNDHEIGHT){
      attKind = 1;
    }
    else if(y >= GROUNDHEIGHT){
      attKind = 2;
    }
    
    if(forcey < -2.6f){
      Anim = AnimUp;
    }
    else if(forcey > 2.6f){
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
  
 public void specialStuff(Fighter Pl, Fighter Opp){
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
  
  public void specialStuff(Fighter Pl, Fighter Opp){
    if(x-40 < Camerabox.x - Camerabox.br/2){
      x = Camerabox.x - Camerabox.br/2 +40;
      forcex *= -1;
    }
    else if(x+40 > Camerabox.x + Camerabox.br/2){
      x = Camerabox.x + Camerabox.br/2 -40;
      forcex *= -1;
    }
  
  }
  
   public void specialOnHit(Fighter Pl, Fighter Opp){
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
  
  public void specialStuff(Fighter Pl, Fighter Opp){
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
  
  public void draw(){
    Anim.handleAnim();
    pushMatrix();
    translate(x,y);
    imageMode(CENTER);
    if(scale > 1){
    scale((scale+1)*0.6f, scale+1);
    }
    image(Anim.Sprites[Anim.curCollumn], 0, 0);
    popMatrix();
    
    if(exTimer > 0){
      exTimer--;
    }
    
  }
  
}
  public void settings() {  size(900, 500, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "sketch_2DFighter_Prototyp" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

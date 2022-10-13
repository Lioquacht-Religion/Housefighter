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
- otg kombos

- light/hardknockdown 
- Air reset/ recovery   //noch nicht wirklich irgendwas davon gemacht //angefangen, animationen anpassen
- juggle state
- wall/ground bounce and wall stick
- stagger

- specialcancels
- zeitbasierte menüelemente
- Counter hits und punish

BUGS:
- Actions können in ihrem collumn stecken bleiben, wenn vorher gecancelt durch anderen Angriff -> Funktion Action.reset() nutzen
- HitStun wird mit jeden hit erhöht -> nicht mehr so, eventuell immer noch buggy
- es ist kein richtiges abrollen füt FB o. DB möglich, wegen der diagonalen -> seltsames doppelspringen
- Blocken geht nicht immer -> scheint gefixt zu sein
- unrechtmäßiges verändern von Pl.multDir kann erst bei seiten wechsel mit gegner behoben werden, im MP
- light attackbutton wird nicht erkannt, wenn nach vorne gelaufen wird und der gegner nicht im standing state ist, bei Seitentausch, wechsel der betroffen laufrichtung-> leigt am lesen der tastatureingaben
- charaktere mit kleineren colboxen können in der corner die kamera verschieben
- Knockdown turned buggy after changing acttimecond

URGENT:
- trennen von effekten auf gegner von einmaligen operationen innnerhalb der Fighterklasse, um mehrer Gegner und spieler zu ermöglichen
- kollision komplett überarbeiten

*/

PApplet p = this;
import processing.pdf.*;
import processing.net.*;
Server s1, s2;
Client c1, c2;
String othersIp;


import net.java.games.input.*;
import org.gamecontrolplus.*;
import org.gamecontrolplus.gui.*;

import ddf.minim.*;
Minim minim;
AudioPlayer[] Soundeffects = new AudioPlayer[8];

ControlIO control;

ControlDevice device1, device2;
PlControl Con1, Con2;

Stage StageBackground; PImage SBextra;

int frame = 0;

final int GROUNDHEIGHT = 460;  // war 620
int initWidth, initHeight;
boolean click = false;


PImage[] Proj_sprs = new PImage[2];

ArrayList<TimedScreenEff> VisEffectsList = new ArrayList<TimedScreenEff>();

char[] inputCharPl1 = {'w', 's', 'd', 'a', 'e', 'f', 'r', 'x', 'y'};
char[] inputCharPl2 = {'o', 'l', 'ö', 'k', 'i', 'j', 'u', 'm', ','};

Fighter Player1, Player2;

Animation HitEff, BlockEff, dustJumpEff, RCEff, BurstEff;

int stageWidth = 0;
  int frameFreeze = 0;
  int slowMoDur, slowMoValue;
  ColRect Camerabox;
  int gameState = 0;
  
//ArrayList<Gamestate> CurGameStates = new ArrayList<Gamestate>() ;
//GS_Handler GSH = new GS_Handler(CurGameStates);
Gamestate CurGameState;

void setup(){
  initWidth = 900;
  initHeight = 500;
  stageWidth = initWidth*2;

  minim = new Minim(this);
  for(int i = 0; i < Soundeffects.length; i++){
    Soundeffects[i] = minim.loadFile("Soundeffekte/sound" + i + ".wav");
  }

    StageBackground = new TrainingStage();
    
  for(int i = 0; i < Proj_sprs.length; i++){
    Proj_sprs[i] = loadImage("Proj_spr" + i + ".png"); 
  }
  
  int[] times0 = {2, 2, 3, 3, 3, 3, 2, 2};
  HitEff = new Animation(times0, 0, 8, "Effekte/HitEff/HitEffekt");
  int[] times1 = {2, 2, 2, 2, 2, 2, 2, 2, 2};
  BlockEff = new Animation(times1, 0, 9, "Effekte/BlockEff/BlockEffekt");
  int[] times3 = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
  dustJumpEff = new Animation(times3, 0, 14, "Effekte/dustJumpEff/n.dustJumpEff");
    int[] times4 = {2, 2, 2, 2, 2, 2};
  RCEff = new Animation(times4, 0, 6, "Effekte/RCEff/RCEff");
    int[] times5 = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
  BurstEff = new Animation(times5, 0, 10, "Effekte/BurstEff/BurstEff");
  
  control = ControlIO.getInstance(this);
  device1 = control.getMatchedDeviceSilent("HFcontroller");
  println("p1Connected");
  device2 = control.getMatchedDeviceSilent("HFcontroller");
  println("p2Connected");
  if(device1 == null){
    println("No, just no");
    //System.exit(-1);
  }
  Con1 = new PlControl(device1, inputCharPl1); Con2 = new PlControl(device2, inputCharPl2); 

    
  //s1 = new Server(this, 10000);//new Server(this, 5204);
  othersIp = "2003:c2:470b:b260:9dfa:8f3a:3dd9:c598";
  
  Player1 = new F_WHaus(initWidth/4, GROUNDHEIGHT, Con1);//.new F_OBStand(initWidth/4, GROUNDHEIGHT, inputCharPl1, device1, new F_OBHaus(initWidth/4, GROUNDHEIGHT, inputCharPl1, device1));
  //false, false);
  //Player1.tint = true;
  Player2 = new F_WHaus(initWidth-initWidth/4, GROUNDHEIGHT, inputCharPl2, device2);
  //true, true);
  Player1.setup();
  Player2.setup();
  
  Camerabox = new ColRect(initWidth/2.0, 0, initWidth, initHeight);
   //CurGameStates.add( new
   CurGameState = new MainMenu();
    
    frameRate(60);
  size(900, 500, P3D);
  //fullScreen(P3D);
  
}
  
void draw(){ 
  background(150);
  //GSH.gameLogic();
  for(int i = 0; i < round(60/frameRate) && frameFreeze <= 0; i++){

        if(slowMoDur % (slowMoValue+1) == 0){
         /* for(int j = CurGameStates.size()-1; 0 <= j; j--){
            CurGameStates.get(j).gameLogic();
          }*/
          CurGameState.gameLogic();
          
        }

      if(slowMoDur > 0){
          slowMoDur--;
      }
       if(frame >= 60){
         frame = 0;
        }
        else{ frame++; }
  }
    
     //for(Gamestate g : CurGameStates){ g.drawVisuals(); }
     CurGameState.drawVisuals();

  click = false;
  
}


Fighter chooseFighter(int index, int x, int y, char[] charinputs){
  //Fighter F = new F_FHaus(x, y, charinputs);
  if(index == 0){
    return new F_FHaus(x, y, charinputs);
  }else if(index == 1){
    return new F_HHaus(x, y, charinputs);
  }else if(index == 2){
    return new F_WHaus(x, y, charinputs);
  }else if(index == 3){
    return new F_PHaus(x, y, charinputs);
  }else if(index == 4){
    return new F_OBHaus(x, y, charinputs);
  }else if(index == 5){
    return new F_Enemy1(x, y, charinputs, true, false);
  }else if(index == 6){
    return new F_KEnem(x, y, charinputs, true, false);
  }else if(index == 7){
    return new F_DEnem(x, y, charinputs, true, false);
  }else if(index == 8){
    return new F_HFBHaus(x, y, charinputs);
  }
   
  
  return new F_FHaus(x, y, charinputs);
}
  

void stop(){
  minim.stop();
}

boolean send = false;
void keyPressed(){
          //for(int j = CurGameStates.size()-1; 0 <= j; j--){ CurGameStates.get(j).keyPressed(); }
          CurGameState.keyPressed();
          Con1.keyPressed(); Con2.keyPressed();
}

void keyReleased(){
          //for(int j = CurGameStates.size()-1; 0 <= j; j--){ CurGameStates.get(j).keyReleased(); }
    CurGameState.keyReleased();
   /* if(key == 'Z' && !send){
      send = true;
      c1 = new Client(this, othersIp, 12346);
    }*/
  Con1.keyReleased(); Con2.keyReleased();
}


void mouseClicked(){
  click = true;
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

void showInputs(PlControl F, float x, float y){
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
    for(int i = 4; i < F.inputs.length; i++){
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
    
  int l_HiLSize = data.getInt("HiLListSize" + i);
    for(int j = 0; j < l_HiLSize; j++){   
      int l_br = data.getInt(HiBox + "br" + j),
      l_ho = data.getInt(HiBox + "ho" + j);
      if(l_br == 0 || l_ho == 0){ println("Hitbux"); continue;}
      ColCircle l_c = new ColCircle(data.getFloat(HiBox + "ax" +j), data.getFloat(HiBox + "ay" + j),  l_br, l_ho, data.getFloat(HiBox + "fx" + j), data.getFloat(HiBox + "fy" + j), -1);
      a.HitBoxCollect.get(i).add(l_c);
      
      /*a.HitBoxCollect.get(i).add(new ColCircle());
      a.HitBoxCollect.get(i).get(j).addx = data.getFloat(HiBox + "ax" + j);
      a.HitBoxCollect.get(i).get(j).addy = data.getFloat(HiBox + "ay" + j);
      a.HitBoxCollect.get(i).get(j).forcex = data.getFloat(HiBox + "fx" + j);
      a.HitBoxCollect.get(i).get(j).forcey = data.getFloat(HiBox + "fy" + j);
      a.HitBoxCollect.get(i).get(j).br = l_br;
      a.HitBoxCollect.get(i).get(j).ho = l_ho;*/
  }
  
  int l_HuLSize = data.getInt("HuLListSize" + i);
    for(int j = 0; j < l_HuLSize; j++){
      int l_br = data.getInt(HuBox + "br" + j),
      l_ho = data.getInt(HuBox + "ho" + j);
      if(l_br == 0 || l_ho == 0){ println("=hubux"); continue;}
      ColCircle l_c = new ColCircle(data.getFloat(HuBox + "ax" +j), data.getFloat(HuBox + "ay" + j),  l_br, l_ho);
      a.HurtBoxCollect.get(i).add(l_c);
      /*a.HurtBoxCollect.get(i).add(new ColCircle());
      a.HurtBoxCollect.get(i).get(j).addx = data.getFloat(HuBox + "ax" +j);
      a.HurtBoxCollect.get(i).get(j).addy = data.getFloat(HuBox + "ay" + j);
      a.HurtBoxCollect.get(i).get(j).br = l_br;
      a.HurtBoxCollect.get(i).get(j).ho = l_ho;*/
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

void delCurGSlist(){
 /* for(Gamestate g : CurGameStates){
    g.deletMe = true;
  }*/
}

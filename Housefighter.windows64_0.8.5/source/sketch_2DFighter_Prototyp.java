import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.pdf.*; 
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


Server s1, s2;
Client c1, c2;
String othersIp;







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

public void setup(){
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
  
  Camerabox = new ColRect(initWidth/2.0f, 0, initWidth, initHeight);
   //CurGameStates.add( new
   CurGameState = new MainMenu();
    
    frameRate(60);
  
  //fullScreen(P3D);
  
}
  
public void draw(){ 
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


public Fighter chooseFighter(int index, int x, int y, char[] charinputs){
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
  

public void stop(){
  minim.stop();
}

boolean send = false;
public void keyPressed(){
          //for(int j = CurGameStates.size()-1; 0 <= j; j--){ CurGameStates.get(j).keyPressed(); }
          CurGameState.keyPressed();
          Con1.keyPressed(); Con2.keyPressed();
}

public void keyReleased(){
          //for(int j = CurGameStates.size()-1; 0 <= j; j--){ CurGameStates.get(j).keyReleased(); }
    CurGameState.keyReleased();
   /* if(key == 'Z' && !send){
      send = true;
      c1 = new Client(this, othersIp, 12346);
    }*/
  Con1.keyReleased(); Con2.keyReleased();
}


public void mouseClicked(){
  click = true;
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

public void showInputs(PlControl F, float x, float y){
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

public void delCurGSlist(){
 /* for(Gamestate g : CurGameStates){
    g.deletMe = true;
  }*/
}
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
  
  public void drawM0(int xstart, int xend, int ystart, int yend){ 
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
  
  public void LevelEditor(){
    if(keyPressed && key == ' '){xEditPos += mouseX - pmouseX; yEditPos += mouseY - pmouseY;}
    
    camera(xEditPos+initWidth/2.0f, yEditPos+initHeight/2.0f, (initHeight/2.0f) / tan(PI*30.0f / 180.0f),
    xEditPos+initWidth/2.0f, yEditPos+initHeight/2.0f, 0,
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
  public void LEkeyReleased(){ ColWidth.keyReleased(); ColHeight.keyReleased();
    if(keyCode == UP) itemstate++; else if(keyCode == DOWN) itemstate--;
    println(itemstate);
  }
  public void EnemyEditor(){
    if(click && mouseButton == LEFT){ 
      Fighter l_F = chooseFighter(PApplet.parseInt(ColWidth.boxText), xEditPos+mouseX, yEditPos+mouseY, inputCharPl2); 
      if(l_F != null ){l_F.setup(); l_F.AI_Controlled = true; Enemies.add(l_F);} }
    else if(click && mouseButton == RIGHT){} 
  }
  public void ColEbeneEditor(){
    //println(xEditPos + " " + yEditPos + " " + mouseX + " " + mouseY);
    if(click && mouseButton == LEFT) ColEbene.add(new ColRect(xEditPos+mouseX, yEditPos+mouseY, 0, 0, PApplet.parseInt(ColWidth.boxText), PApplet.parseInt(ColHeight.boxText) ) );
    else
    if(click && mouseButton == RIGHT){ 
      
      for(int i = ColEbene.size()-1; i >= 0; i--){ 
        ColRect c = ColEbene.get(i);
        if(recPointCol(xEditPos+mouseX, yEditPos+mouseY, c.x, c.y- c.ho, c.br, c.ho)){ ColEbene.remove(i); break;}
      }
      
    }
    
  }
    public void ColEbeneEditor2(int xstart, int xend, int ystart, int yend){
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
  
  public void SprM0Editor(int xstart, int xend, int ystart, int yend){
       // int srcX = -(roomWUnits/2*cellsize); int srcY = -(roomHUnits/2*cellsize);
       imageMode(CORNER);
       if(PApplet.parseInt(ColWidth.boxText) < SprM0Sprites.size() && PApplet.parseInt(ColWidth.boxText) >= 0 )image(SprM0Sprites.get(PApplet.parseInt(ColWidth.boxText)), xEditPos, yEditPos);
       println((xEditPos + mouseX)+ roomWUnits/2*60, (yEditPos +mouseY) + roomHUnits/2*60);
    for(int i = xstart; i < xend && i >= 0 && i < SprIDMap0.length; i++){
      for(int j = ystart; j < yend && j >= 0 && j < SprIDMap0[i].length; j++){

        
            if(mousePressed && mouseButton == LEFT && recPointCol( (xEditPos + mouseX)+ roomWUnits/2*60, (yEditPos +mouseY) + roomHUnits/2*60, 
            cellsize*i, cellsize*j, cellsize, cellsize ) ){ SprIDMap0[i][j] = PApplet.parseInt(ColWidth.boxText); print("click"); return;}
    else
    if(mousePressed && mouseButton == RIGHT && recPointCol( (xEditPos + mouseX)+ roomWUnits/2*60, (yEditPos +mouseY) + roomHUnits/2*60, 
            cellsize*i, cellsize*j, cellsize, cellsize  ) ){ SprIDMap0[i][j] = -1; return;}
        
        
      }
    }
  }
  
  public void loadRoom(){}
  public void saveRoom(){}
  
  
    public float[] colCheckEnv2(Fighter F, int xstart, int xend, int ystart, int yend){
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
  
  public float[] colCheckEnv(Fighter F){
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
  
    public void keyPressed(){   for(Fighter P : Players){ P.keyPressed(); } 
  for(Fighter E : Enemies){ E.keyPressed(); }
}
  public void keyReleased(){  for(Fighter P : Players){ P.keyReleased(); } 
  for(Fighter E : Enemies){ E.keyReleased(); }
  if(key == 'B' && !this.Edit){ this.Edit = true; println("Editmode on");} else if(key == 'B' && this.Edit == true){ this.Edit = false; println("Editmode off");}
  if(Edit){LEkeyReleased();}
}
  
}
abstract class Fighter{

  String name = "poopdipoop";
  final boolean RIGHT = false;
  final boolean LEFT = true;
  
  public void printPDF(){
    float scale = 0.5f;
    PGraphics pdf = createGraphics(2000, 9000, PDF, name + ".pdf");
    imageMode(CORNER);
    int nextHeight = 30;
    pdf.beginDraw();
    pdf.background(255);
    pdf.textSize(20);
    pdf.fill(200,0,0); 
    pdf.text(name + " - Datasheet", 20, 10);
    
    for(int i = 0; i < ActionList.size(); i++){
      for(int j = 0; j < ActionList.get(i).size(); j++){
        Action a = ActionList.get(i).get(j);
        
        if(a.AttAnim != null){
          int st = 0, at = 0, et = 0;
          for(int k = 0; k < a.HitBoxCollect.size(); k++){
            if(a.HitBoxCollect.get(k).size() == 0 && at == 0) st += a.whenToUpdBoxs[k];
            else if(a.HitBoxCollect.get(k).size() > 0) at += a.whenToUpdBoxs[k];
            else if(a.HitBoxCollect.get(k).size() == 0 && at > 0) et += a.whenToUpdBoxs[k];
          }
          String ActData = a.datnam + ": Damage: " + a.damage + "; AttWeight: " + a.attWeight + "; Hitstun: " + a.affHitStunT + "; Blockstun: " + a.affBlockStunT + "; Startup: " + st + "; Active: " + at + "; Endlag: " + et;
          

          Animation Anim = a.AttAnim;
          int heightExtra = 0;
          
          for(int k = 0, k2 = 0; k < Anim.Sprites.length; k++){
            if( (Anim.Sprites[k].width*scale*k)/(heightExtra+1) > pdf.width - (Anim.Sprites[k].width*scale) ){
              heightExtra++; k2 = k;
            }

            pdf.image(Anim.Sprites[k], Anim.Sprites[k].width*scale*(k-k2), nextHeight + Anim.Sprites[0].height*scale * heightExtra, Anim.Sprites[k].width*scale, Anim.Sprites[k].height*scale);
                pdf.textAlign(CENTER, CENTER); 
                pdf.textSize(40); pdf.fill(200, 0, 0);
            pdf.text(Anim.changeTimes[k], Anim.Sprites[k].width*scale*(k-k2)+30, nextHeight + Anim.Sprites[0].height*scale * heightExtra);
                pdf.textSize(34); pdf.fill(150);
            pdf.text(Anim.changeTimes[k], Anim.Sprites[k].width*scale*(k-k2)+30, nextHeight + Anim.Sprites[0].height*scale * heightExtra);
          }

          nextHeight += Anim.Sprites[0].height * scale * (heightExtra+1) + 80;
          heightExtra = 0;
          pdf.textSize(20); pdf.textAlign(CORNER); pdf.text(ActData, 10, nextHeight-60);
        }
                
      }
    }
    
    pdf.dispose();
    pdf.endDraw();
  }
  
  PImage[] Sprs;
  
  PlControl PlContr = new PlControl();
  ControlDevice device = null; ControlButton LightBut, MediumBut, HeavyBut, RCBut, EMBut; ControlHat DPad; ControlSlider LStick_X, LStick_Y; boolean[] pInputs = new boolean[4];
  boolean[] inputs = new boolean[9]; //up , down, right, left, Roof, Mid, Base, utility(RC), Special;
  boolean[] firstPressInp = new boolean[inputs.length];
  boolean[] firstPressDia = {true, true, true, true};
  int[] inputChargeT = new int[inputs.length];
  char[] charinputs;
  InputRecord Recorder = new InputRecord();
  
  boolean AI_Controlled = false;
  boolean tint = false;
  boolean easyMode = false;
  
  IntList inputbufferDir = new IntList();
  ArrayList<IntList> inputbufferBut = new ArrayList<IntList>();
  int curInputTimer = 0;
  
  int comboCount = 0;
  
  int[] FBmotion = {2, 3, 6}, DPmotion = {6, 2, 3}, fDashmotion = {6, 6}, bDashmotion = {4, 4};
  
  float x, y;
  PVector Force = new PVector(0, 0);
  float gforce = 0;
  int dirMult = 1;
  float m = 1;
  
  ColRect CollisionBox;
  
  boolean facing = false;
  boolean grounded = false;
  boolean crouching = false;
  
  int maxHP = 200, curHP = maxHP;
  int maxSuper = 1000, curSuper = 0;
  int maxAirActions = 1, curAirActions = maxAirActions;
  
  int HitStunT = 0;  //wird das noch verwendet?
  int cancelWindow = 0, throwInvu = 0;
  boolean counterState = false, punishState = false;
  
  ArrayList<ColCircle> HurtBoxes = new ArrayList<ColCircle>();
  ArrayList<ColCircle> HitBoxes = new ArrayList<ColCircle>();
  
  ArrayList<Projectile> Projectiles = new ArrayList<Projectile>();
  
  float[] AI_InputsScore = {.1f, .1f, .2f, .2f, .8f, .3f, .3f, .0f, .3f};
  int pHP = 200, pOppHP = 200; float abstand = 100;
  
  Animation CurAnimation = null;
  
  Action CurAction = new Action();
  
  Action Standing, Crouching, Falling, Landing, Turning;
  Action Knockdown, KDstandup, softKD, WallStick, WallBounce, GroundBounce, Juggle, Stagger;
  boolean[] hitstates = new boolean[5];
  Animation AnimKD, AnimKDreturn, AnimAirHit; 
  Action BeingGrapped;
  Action InHitStun;
  Animation HHit, LHit;
  Action Jumping, fDiaJump, bDiaJump, Jumping2, fDiaJump2, bDiaJump2;
  Action Blocking;
  Animation HBlock, LBlock;
  Animation wBflightB, wBstick, wBflightF,
    gBflightD, gBstick;
  
Action fWalk, bWalk, FDash, BDash, airFDash, airBDash;

Action EditAction, NormalThrow, AirThrow;

  Action LightNormal, MidNormal2, HeavyNormal, cr_LightNormal, cr_MidNormal2, cr_MidNormal, cr_HeavyNormal, j_LightNormal, j_MidNormal, j_HeavyNormal, j_DustNormal;

  ArrayList<ArrayList<Action>> ActionList = new ArrayList<ArrayList<Action>>();
  
  public void checkSingleActList(Fighter Opp, ArrayList<Action> ActList){
    for(Action a : ActList){
      if(a != null){
        boolean l_check = false;
      for(int i = 0; i < a.Conds.length; i++){
          if(a.Conds[i].cond(this, Opp) && (CurAction.attWeight < a.attWeight || (a.selfCancel && CurAction == a) ) ){
            a.Conds[i].EffectIfCond(this, Opp);
            l_check = true;
          }else {l_check = false; break;}
      }
      if(l_check){
        changeAction(a); 
        if(cancelWindow > 0){ cancelWindow = 0; gforce = 0;}
      }
    }
    }
  }
  
  Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, FDash, BDash, Jumping, fDiaJump, bDiaJump},
  {cr_LightNormal, cr_MidNormal, cr_HeavyNormal},
  {j_LightNormal, j_MidNormal, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2},
  {}
};
  public void fillActionsList(Action[][] ActTab){
    ActionList = new ArrayList<ArrayList<Action>>();
    for(int i = 0; i < ActTab.length; i++){
      ActionList.add(new ArrayList<Action>());
      for(int j = 0; j < ActTab[i].length
      ; j++){
        ActionList.get(i).add( ActTab[i][j]);       
      }
    }
    
  }
  

  
  Fighter(){
  }
  
  Fighter(int x, int y, char[] charinputs){
    this.x = x;
    this.y = y;
    //this.charinputs = charinputs;
    PlContr = new PlControl(charinputs);
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
    PlContr = new PlControl(device, charinputs);
  }
  
  Fighter(int x, int y, PlControl PlContr){
    this.x = x;
    this.y = y;  
    this.PlContr = PlContr;
  }
  
  Fighter(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten //WARNING: not in use anymore
    this( x, y, charinputs);
    m = F.m; maxHP = F.maxHP; curHP = maxHP;
    maxSuper = F.maxSuper; curSuper = 0;
    maxAirActions = F.maxAirActions; curAirActions = maxAirActions;
    ColRect cf = F.CollisionBox;
    CollisionBox = new ColRect(cf.x, cf.y, cf.br, cf.ho);
    Sprs = F.Sprs;
    for(int i = 0; i < F.ActionList.size(); i++){
      ActionList.add( new ArrayList<Action>() );
      
      for(int j = 0; j < F.ActionList.get(i).size(); j++){
        // copy references Animation( Spr-Array, times array, ), Cond Array, Hit and Hurtboxes, times array, force array
        //println(F.ActionList.get(i).get(j).datnam);
        ActionList.get(i).add(new Action( F.ActionList.get(i).get(j) ) );
      }
      
    }
    
    copyStdActions(F);
    
  }
  
  public void copyStdActions(Fighter F){
        Standing = new Action(F.Standing); Crouching = new Action(F.Crouching);
    Knockdown = new Action(F.Knockdown); softKD = new Action(F.softKD); WallStick = new Action(F.WallStick); WallBounce = new Action(F.WallBounce);
    Juggle = new Action(F.Juggle); Stagger = new Action(F.Stagger); BeingGrapped = new Action(F.BeingGrapped);
    InHitStun = new Action(F.InHitStun); HHit = new Animation(F.HHit); LHit = new Animation(F.LHit);
    Blocking = new Action(F.Blocking); HBlock = new Animation(F.HBlock); LBlock = new Animation(F.LBlock);
    Jumping = new Action(F.Jumping); fDiaJump = new Action(F.fDiaJump); bDiaJump = new Action(F.bDiaJump);
    Jumping2 = new Action(F.Jumping2); fDiaJump2 = new Action(F.fDiaJump2); bDiaJump2 = new Action(F.bDiaJump2); // HHaus has no double jump -> reason for nullptrexcept
    fWalk = new Action(F.fWalk); bWalk = new Action(F.bWalk); FDash = new Action(F.FDash); BDash = new Action(F.BDash); // maybe adding costume constructor
  }
  
  public void setConDevice(){
    DPad = device.getHat("DPAD");
    LightBut = device.getButton("LIGHT");
    MediumBut = device.getButton("MEDIUM"); 
    HeavyBut = device.getButton("HEAVY");
    RCBut = device.getButton("RC");
    EMBut = device.getButton("EM");
    LStick_X = device.getSlider("LSTICK-X"); 
    LStick_Y = device.getSlider("LSTICK-Y");
  }
  
  public void stunStatesAnimSetup(){
     AnimKD = new Animation(new int[]{2, 2, 2, 2, 20} , 0, 5,"FHouse/FH-hKD/FH-Knockdown"); 
     AnimKD.loop = false;
     AnimKDreturn = new Animation(new int[]{2, 2, 2, 2} , 0, 4,"FHouse/FH-hKD/FH-KDreturn"); 
     AnimAirHit = new Animation(new int[]{4, 4, 4, 4, 4} , 0, 5,"FHouse/FH-sKD/FH-airHitstun");
     AnimAirHit.loop = false;
  }
  
  public void setup(){
    stunStatesAnimSetup();
    fillActionsList(ActTab);
    
        int[] ani10 = {2};
  InHitStun = new Action(ani10, 0, 0, 0, 0, Action.HITSTATE, true, true, false, false);
  InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160));
  
  KDstandup = new Action(AnimKDreturn, 0, 0, 0, 0, Action.HITSTATE, true, false, false, false);
  KDstandup.updFrameDataArr(0, 16); 
  KDstandup.updFrameDataArr_float(0, 0, 0);

  
  //int[] ani11 = {11, 11};
  Knockdown = new Action(AnimAirHit, 0, 0, 0, 0, Action.HITSTATE, true, true, false, false);
  Knockdown.ActEffs = new ActEffect[]{
    new ChangeAnimTo(AnimAirHit, 1, new ActTimeCond2(0, 1) ),
    new ChangeAnimTo(AnimKD, 1, new ActTimeCond(0, 4, 0, 100), new Grounded(), new AnimCheck(AnimAirHit) 
    ),
    //new ChangeAnimTo(AnimKD, 1, new ActTimeCond(1, 1), new AnimCheck(AnimAirHit, true) ),
    //new ChangeAnimTo(AnimAirHit, 1, new ActTimeCond(1, 39) ) 
    new ChangeActTo(KDstandup, 1, new ActTimeCond(1, 40, 1, 80), new Grounded() )
  };
 //new ChangeAnimTo(AnimKD, 1, new ActTimeCond(1, 1), new AnimCheck(AnimAirHit), new Grounded() ),
  Knockdown.HurtBoxCollect.get(0).add(new ColCircle( 0, -40, 180, 80, 0, 0, -1));
  Knockdown.updFrameDataArr(0, 80); 
  Knockdown.updFrameDataArr_float(0, 0, -4);
  Knockdown.addAllLists(1, 80, 0, 0);

   int[] ani13 = {2,2};
  softKD = new Action(AnimAirHit,
  0, 0, 0, 0, Action.HITSTATE, true, true, false, false);
  softKD.ActEffs = new ActEffect[]{
    //new ChangeAnimTo(AnimKD, 1, new ActTimeCond(0, 0) ),
     new ChangeAnimTo(AnimAirHit, 1, new ActTimeCond2(0, 0) ), 
    new ChangeActTo(KDstandup, 1, new Grounded() )    
  };
  softKD.gravMult = 0.92f;
  softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  softKD.updFrameDataArr(0, 1); 
  softKD.updFrameDataArr_float(0, 0, 0);
  softKD.addAllLists(1, 40, 0, 0);
  softKD.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  
  //Opp.WallStick, Opp.WallBounce, Opp.Juggle, Opp.GroundBounce, Opp.Stagger
  
  wBflightB = new Animation(new int[]{2} , 0, 1, "FHouse/FH-spHitStates/FH-wBounce/FH-wBounce1p");
  wBstick = new Animation(new int[]{4, 40} , 0, 2, "FHouse/FH-spHitStates/FH-wBounce/FH-wBounce2p");
  wBflightF = new Animation(new int[]{2} , 0, 1, "FHouse/FH-spHitStates/FH-wBounce/FH-wBounce3p");
  
  gBflightD = new Animation(new int[]{2} , 0, 1, "FHouse/FH-spHitStates/FH-gBounce/FH-gBounce1p");
  gBstick = new Animation(new int[]{4, 40} , 0, 2, "FHouse/FH-spHitStates/FH-gBounce/FH-gBounce2p");
  gBstick.loop = false;
  
    WallStick = new Action(wBstick, 0, 0, 0, 0,Action.HITSTATE, false, false, false, false);
  WallStick.gravMult = 0.92f;
  WallStick.ActEffs = new ActEffect[]{new ChangeAnimTo(wBflightB, 1, new ActTimeCond2(0, 0)), 
  new ChangeAnimTo(wBstick, 1, new CamWallTouch()),  new SetForce(-20, 0, 1, new CamWallTouch() ), };
  WallStick.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  WallStick.updFrameDataArr(0, 58); WallStick.updFrameDataArr_float(0, -40, -10);
  //WallStick.addAllLists(1, 40, -5, 0);
  //WallStick.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  
  WallBounce = new Action(wBflightB, 0, 0, 0, 0,Action.HITSTATE, true, true, false, false);
  WallBounce.gravMult = 0.92f; 
  WallBounce.ActEffs = new ActEffect[]{new ChangeAnimTo(wBflightB, 1, new ActTimeCond2(0, 0)), new ChangeAnimTo(wBstick, 1, new CamWallTouch()),
  new TimerEff( 10, new Condition[]{new CamWallTouch()}, new SetForce(34, -6, 1, new CamWallTouch() ), new ChangeAnimTo(wBflightF, 1) ) };  //new WallBounceEff(1, new Condition[]{new Condition()})};
  WallBounce.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  WallBounce.updFrameDataArr(0, 46); WallBounce.updFrameDataArr_float(0, -40, -10);
  
  GroundBounce = new Action(gBflightD, 0, 0, 0, 0,Action.HITSTATE, true, true, false, false);
  //WallBounce.gravMult = 0.92; 
  GroundBounce.ActEffs = new ActEffect[]{new ChangeAnimTo(gBflightD, 1, new ActTimeCond2(0, 0)), 
  new ChangeAnimTo(gBstick, 1, new Grounded()), new TimerEff(10, new Condition[]{new Grounded()}, new SetForce(0, -20, 1, new Grounded()))};
  GroundBounce.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  GroundBounce.updFrameDataArr(0, 40); GroundBounce.updFrameDataArr_float(0, 0, 20);
  
  Juggle = new Action(new Animation(new int[]{6, 6, 6, 6, 6} , 0, 5, "FHouse/FH-spHitStates/FH-juggle/FH-juggle"), 0, 0, 0, 0, Action.HITSTATE, false, false, false, false);
  Juggle.gravMult = 1; Juggle.ActEffs = new ActEffect[]{
  new ForceAddEff(0, 0, false), new ForceAddEff(0, 4, true), new GravEff(0, 0, false), new GravEff(0, 4, true), new ChangeActTo(softKD, 1, new ActTimeCond2(1, 24))}; 
  Juggle.updFrameDataArr(0, 5); Juggle.updFrameDataArr_float(0, -2, -20);
  Juggle.addAllLists(1, 48, -3, 0);
  Juggle.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1)); Juggle.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  
  Stagger = new Action(new Animation(new int[]{6, 6, 6}, 25, 3, "FHouse/FH-spHitStates/FH-stagger/FH-stagger2p"), 0, 0, 0, 0, Action.HITSTATE, false, false, false, false);
  Stagger.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Stagger.updFrameDataArr(0, 90); Stagger.updFrameDataArr_float(0, -5, 0);
  
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, Action.HITSTATE, false, true, false, false); BeingGrapped.collision = false;
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 200, 160, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
    float[][] xyGrab = {{600, -80},
                      {320, -320},
                      {300, -100}, 
                    {400, 40}};
  Action NormalGrab = new Action("FH-st-idle", 
  new Animation( new int[]{6, 4, 2, 4, 4, 4, 4, 4, 4, 6, 4, 2, 2} , 70, 13, "FHouse/FH-nthrow/p2/FH-nthrow"), 
  new Condition[]{new Condition()}, 
  2, 0, 0, Action.GRAB, 100, true, false, false, false); NormalGrab.hitCancel = false;
 NormalGrab.ActEffs = new ActEffect[]{new GrabEff(xyGrab , new int[]{20,10,5,20}), new PutInHitSt(30, 10, new ActTimeCond2(0, 18) )};
  NormalGrab.updFrameDataArr(0, 20); 
  NormalGrab.updFrameDataArr_float(0, 2, 0);
  NormalGrab.addAllLists(1, 20, 5, 0);
  NormalGrab.addAllLists(2, 20, 5, 0);
  NormalGrab.addAllLists(3, 2, 0, 0);

  Condition[] Cond00 ={new facingCond(1), new fPButCond(8)};//, new fPButCond(5)  };// ;
  NormalThrow = new Action("FH-nthrow", 
  new Animation( new int[]{2, 2, 6, 4, 2} , 70, 5, "FHouse/FH-nthrow/p1/FH-nthrow"), 
 Cond00, 
  10, 0, 0, Action.GRAB, 1, true, false, false, false);
 NormalThrow.ActEffs = new ActEffect[]{new ChangeActTo(NormalGrab, 0, new OppCheck(new Grounded()))};
 NormalThrow.hitCancel = false;
 
     float[][] xyGrab1 = {{200, -80},
                      {200, -80}};
  Action AirThrowP2 = new Action("FH-airthrowp2", 
  new Animation( new int[]{4, 4, 2, 6, 2, 6, 4, 4, 2}, 50, 40, 9, "FHouse/FH-airThrow/p2/FH-airThrow"), 
  new Condition[]{new Condition()}, 
  10, 0, 0, Action.GRAB, 100, false, false, false, false); AirThrowP2.hitCancel = false;
 AirThrowP2.ActEffs = new ActEffect[]{new GrabEff(xyGrab1 , new int[]{10,2}), new PutInHitSt(30, 10, new ActTimeCond2(0, 18) ), new OppEff(new SetForce(-4, 20, 1), 1, new ActTimeCond2(0,18) ) };

  AirThrow = new Action("FH-airthrow", 
  new Animation( new int[]{2, 2, 6, 4, 4, 2}, 20, 40, 6, "FHouse/FH-airThrow/FH-airThrow"), 
 new Condition[]{new facingCond(1), new fPButCond(8), new InAir()}, 
  10, 0, 0, Action.NOTHING, 10, true, true, false, false);
 AirThrow.ActEffs = new ActEffect[]{new ChangeActTo(AirThrowP2, 0, new OppCheck( new InAir() ) )};
 AirThrow.hitCancel = true;
 
 Falling = new Action(new Animation(new int[]{4, 4, 4}, 0, 3, "FHouse/FH-jumpfall/FH-jfall"), 0, 0, 0, 0, true, true, false, false);
 Falling.updFrameDataArr(0, 0); Falling.updFrameDataArr_float(0, 0, 0);
 Falling.HurtBoxCollect.get(0).add(new ColCircle(0, -100, 180, 180));
 Falling.resetAnim = false; //Falling.AttAnim.loop = false;
 //Falling.ActEffs = new ActEffect[]{new FirstTimeEff(new ResAnimEff(1))};
 
 Landing = new Action(new Animation(new int[]{3}, 0, 1, "FHouse/FH-sttocr/FH-sttocr"), 0, 0, 0, 0, true, true, false, false);
 Landing.updFrameDataArr(0, 3); Landing.updFrameDataArr_float(0, 0, 0);
 Landing.HurtBoxCollect.get(0).add(new ColCircle(0, -100, 180, 180));
 Landing.resetAnim = false; Landing.attWeight = 0;
 
 Turning = new DirChangeAct("FH-st-idle", new Animation(new int[]{3,3,3},  0, 3, "FHouse/FH-turning/FH-turning"),new Condition[]{}, 0, 0, 0, 0, 0, true, false, false, false);
 Turning.updFrameDataArr(0, 9); Turning.updFrameDataArr_float(0, 0, 0);
 Turning.HurtBoxCollect.get(0).add(new ColCircle(0, -100, 180, 180));
 Turning.attRank = Action.HITSTATE;//Turning.ActEffs = new ActEffect[]{};
  
  specSetup();
  
  }
  
  public abstract void specSetup();
  
  public void standingStateReturn(Fighter Opp){
    if(y == GROUNDHEIGHT || CollisionBox.bottom) curAirActions = maxAirActions;
    
      if((y >= GROUNDHEIGHT || CollisionBox.bottom) && (CurAction.attKind == 4 || CurAction == Falling) ){
        changeAction(Landing); Falling.AttAnim.Reset();
      }
      else if((y == GROUNDHEIGHT || CollisionBox.bottom) && inputs[1]){
        if(CurAction == Standing) changeAction(Landing);
        else CurAction.playAction2(this, Opp, Crouching);
      }
      else if (y == GROUNDHEIGHT || CollisionBox.bottom){
        if(CurAction == Crouching) changeAction(Landing);
        else CurAction.playAction2(this, Opp, Standing);
      }
      else if(y < GROUNDHEIGHT || !CollisionBox.bottom){
        CurAction.playAction2(this, Opp, Falling);
      }
      
  }
  
  protected void gameLogic(Fighter Opp){
    if(Recorder != null){ 
      if(Recorder.state != 0 ) Recorder.work(this);
      if(Recorder.state != 2) PlContr.deviceInput();
      PlContr.draw();
    }
      
    for(int i = 0; i < PlContr.inputs.length;i++) this.inputs[i] = PlContr.inputs[i];
    
    if(AI_Controlled){ 
      if(frame % 6 == 0){      
        AI_Controll(Opp);
        AIControll2(Opp);
      }
      AIControll3(Opp);
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
    controll(Opp);
   
    updateColList();
    updateProjectiles(Opp);
    
    if(CurAnimation != null){
      CurAnimation.handleAnim();
        }
    
    
        curHP = constrain(curHP, 0, maxHP);
    curSuper = constrain(curSuper, 0, maxSuper);

    if(cancelWindow > 0){
      cancelWindow--;
    }
    
   extraStuff();
  }
 
  protected void draw(){
    
   sprite();
        
    pushMatrix();
    translate(x, y);
    if(facing == LEFT){
      scale(-1, 1);
    }
    
    if(tint) tint(240, 153, 255);
    if(HurtBoxes.size() == 0 && frame % 4 == 0) tint(105, 105, 105, 100);
    //if(throwInvu > 0 && frame % 4 == 3) tint(105, 205, 105, 100);
    imageMode(CENTER);
    if(CurAnimation != null){
      
    if(tint) tint(255, 153, 150);
    if(HurtBoxes.size() == 0 && frame % 4 == 0) tint(105, 105, 105, 100);
    //if(throwInvu > 0 && frame % 4 == 3) tint(195, 255, 195, 100);
    rotate(CurAnimation.rot);
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
    if(Recorder != null && Recorder.state != 0 ){
      Recorder.work(this);
    }
    if(device != null && Recorder.state != 2){
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
      inputs[8] = EMBut.pressed();
    }
    
    
  }
  
  public void keyPressed(){
    //PlContr.keyReleased();
    inputs = inputsKey(true, PlContr.charinputs, PlContr.inputs);
    //for(int i = 0; i < PlContr.inputs.length;i++) this.inputs[i] = PlContr.inputs[i];
  }
  public void keyReleased(){
    if(!AI_Controlled) //PlContr.keyReleased();
    inputs = inputsKey(false, PlContr.charinputs, PlContr.inputs);
    //for(int i = 0; i < PlContr.inputs.length;i++) this.inputs[i] = PlContr.inputs[i];
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
 
  
public void fighterActionsExtra(){}

public void fighterActions(Fighter Opp){
      if((CurAction == Standing || CurAction == bWalk|| CurAction == fWalk || (CurAction.hitCancel && cancelWindow > 0 && !inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){  
        if(cancelWindow <= 0 && !inputs[1]){ // not able to walk while holding down
           normalWalk();
        }
           jump();
           dash();    
           st_Normals();
           checkSingleActList(Opp, ActionList.get(0));
           easyControl(0);
       }
       else if( (CurAction == Crouching || (CurAction.hitCancel && cancelWindow > 0 && inputs[1]) ) && (y == GROUNDHEIGHT || CollisionBox.bottom)){
         cr_Normals();
         checkSingleActList(Opp, ActionList.get(1));
         easyControl(0);
       }
       else if( (CurAction == Falling || (CurAction.hitCancel && cancelWindow > 0) || CurAction.allButSelfCancel ) && y < GROUNDHEIGHT && !CollisionBox.bottom){
         airDash();
         jump();
         j_Normals();
         checkSingleActList(Opp, ActionList.get(2));
         easyControl(2);
       }
       else if(CurAction.attRank < 1){
         checkSingleActList(Opp, ActionList.get(3));
         //easyControl(3);
       }
       
       fighterActionsExtra();
       

       
}

public void easyControl(int z){
       if(easyMode){
                  int inputNum = 0; Action[] easyActs = new Action[7];
           for(int i = 0; i < ActionList.get(z).size() && inputNum < easyActs.length; i++){
             Action a = ActionList.get(z).get(i);
             Condition[] c = a.Conds;
             for(int j = 0; j < c.length; j++){
               if( (c[j] instanceof comfPButC || c[j] instanceof dirCombCond || c[j] instanceof OrCond || c[j] instanceof ChargeDirCheck) && inputNum < easyActs.length){
                 easyActs[inputNum] = a;
                 inputNum++;
                 break;
               }
             }
           }
           
           for(int i = 0; i < 7; i++){ if(inputs[8] && inputs[i] && easyActs[i] != null) changeAction(easyActs[i]); }
           
       }
}

public void st_Normals(){}; public void cr_Normals(){}; public void j_Normals(){};


public void normalWalk(){
      if(inputs[2]){
      if(dirMult == 1){
        CurAction.reset();
        CurAction = fWalk;
        curSuper++;
      }
      else if(dirMult == -1){
        CurAction.reset();
        CurAction = bWalk;
      }
    }
    
    if(inputs[3]){
       if(dirMult == -1){
        CurAction.reset();
        CurAction = fWalk;
        curSuper++;
      }
      else if(dirMult == 1){
        CurAction.reset();
        CurAction = bWalk;
      }
    }
}
public void jump(){
  if(cancelWindow <= 0 || CurAction.jumpCancel) ; else return;
  int[] fJump = {9};
  int[] bJump = {7};
  Action l_ResultAction = CurAction;
  if(y == GROUNDHEIGHT || CollisionBox.bottom){
    if(compareBufferWithCombAtt(fJump) && inputs[0]){
      l_ResultAction = fDiaJump;
    }
    else
    if(compareBufferWithCombAtt(bJump) && inputs[0]){
      l_ResultAction = bDiaJump;
    }
    else
      if(inputs[0]
      ){
      l_ResultAction = Jumping;
    }
    
  }
  else
  if(curAirActions > 0){
        if(compareBufferWithCombAtt(fJump) && firstPressInp[0]){
      l_ResultAction = fDiaJump2;
    }
    else
    if(compareBufferWithCombAtt(bJump) && firstPressInp[0]){
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
       
       if(cancelWindow > 0) cancelWindow = 0;
    }
  
}
public void dash(){
  if(cancelWindow <= 0 || CurAction.dashCancel) ; else return;
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

public void airDash(){
  if(airFDash == null || airBDash == null){dash(); return;}
  
    if(cancelWindow <= 0 || CurAction.dashCancel) ; else return;
  if(curAirActions > 0){
          if(compareBufferWithCombAtt(fDashmotion)){
            CurAction.reset();
          CurAction = airFDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
    if(compareBufferWithCombAtt(bDashmotion)){
            CurAction.reset();
          CurAction = airBDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
  }
}

  public void faultlessDefense(){
                    //faultless Defense //TODO: Auslagern von Blockaktion in allgemeine Actlist
    if( (CurAction == Standing || CurAction == Falling || CurAction == Blocking || CurAction == Crouching || CurAction == bWalk) && inputs[8] 
    && ( (inputs[2] && dirMult == -1) || inputs[3] && dirMult == 1 ) && curSuper > 0){ 
      if(inputs[1]) Blocking.AttAnim = LBlock; else Blocking.AttAnim = HBlock;
      Blocking.whenToUpdBoxs[0] = 1; changeAction(Blocking);
      VisEffectsList.add(new VisualEffect(x, y - 100, BurstEff, dirMult)); curSuper -= 4;
  }
  
  }
  
  public void controll(Fighter Opp){ 

  
        if(HitStunT == 0 && CurAction != Blocking && CurAction != BeingGrapped && CurAction != InHitStun && CurAction.attRank != Action.HITSTATE){
  
       fighterActions(Opp);
       
    faultlessDefense();
       
    if( CurAction != Standing && inputs[7] && firstPressInp[7] && curSuper >= 500){
      changeAction(Standing);
      curSuper -= 500; slowMoDur = 40; slowMoValue = 4;
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
                changeAction(KDstandup);
                      Soundeffects[1].cue(0);
      Soundeffects[1].play();
      VisEffectsList.add(new VisualEffect(x, y, RCEff, 0));
                break;
              }
    }
    }
    
            if(CurAction == softKD && softKD.curCollumn == 1 && !new Grounded().cond(this, Opp)){
      for(int i = 0; i < inputs.length; i++){
              if(inputs[i] && firstPressInp[i]){
                changeAction(Standing);
                gforce = 0; Force.y = 0;
                if(inputs[0]){Force.y = -18;}
                if(inputs[1]){Force.y = 12;}
                if(inputs[2]){Force.x = 16;}
                if(inputs[3]){Force.x = -16;}
                slowMoDur = 9; slowMoValue = 3;
                      Soundeffects[1].cue(0);
      Soundeffects[1].play();
      VisEffectsList.add(new VisualEffect(x, y, RCEff, 0));
                break;
              }
    }
    }
    else if(CurAction == softKD && softKD.curCollumn == 1 && new Grounded().cond(this, Opp)){
                changeAction(Standing);
    }
    
    if(throwInvu > 0) throwInvu--;
    //if(CurAction == InHitStun || CurAction == Blocking || CurAction == softKD || CurAction == Knockdown || CurAction == KDstandup || CurAction == BeingGrapped){}
        
    if(CurAction.gravityActive && !CollisionBox.bottom && y < GROUNDHEIGHT){
      Force.y += gforce;
    }
    else{gforce = 0;}
    
    if( y < GROUNDHEIGHT && CurAction.gravityActive && !CollisionBox.bottom && gforce <= 2.5f //&& cancelWindow <= 0
    ){
      //gforce += 0.2 * m + 0.01;
      gforce += abs(Force.y) * 0.01f * m + 0.02f  ;
      gforce *= CurAction.gravMult;
    }
    else if(y > GROUNDHEIGHT){
      y = GROUNDHEIGHT;
      Force.y = 0;
      gforce = 0;
    }
    
    //Friction
    if(new Grounded().cond(this, this)){
      Force.x = Force.x * 0.95f * CurAction.fricMult;
    }
    else{ Force.x = Force.x * 0.97f * CurAction.fricMult; }
    
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
  
  public void updatePos(float[] maxAllowedArr//ArrayList<ColRect> ColRects
  ){
    float maxAllowedFx = maxAllowedArr[0], maxAllowedFy = maxAllowedArr[1];
    ColRect c = CollisionBox; 
    if(-0.1f > Force.x || Force.x > 0.1f){
      if(c.rside && maxAllowedFx > 0 && Force.x > 0){
        x += abs(maxAllowedFx)-1;
      }
      else
      if(c.lside && maxAllowedFx < 0 && Force.x < 0){
        x -= abs(maxAllowedFx)-1;
      }
      else
     {x += Force.x;}
      
    }
    /*if(!CollisionBox.bottom ){
      y += Force.y;
    }*/
      if(c.bottom && maxAllowedFy > 0 && Force.y > 0){
        y += abs(maxAllowedFy); Force.y = 0;
      }
      else
      if(c.top && maxAllowedFy < 0 && Force.y < 0){
        y -= abs(maxAllowedFy)-1;
      }
      else
     {y += Force.y;}
    
    updateColList();
    c.setColBools(false);
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
    stroke(200, 0, 0);
    line(x, y, x + Force.x*2, y + Force.y*2);
  }
  
  public void drawColList(ArrayList<ColCircle> ColList){
    for(ColCircle c : ColList){
      c.draw(dirMult);
    }
  }
  
  
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
  
  public boolean ChargeDirCheck(int chargeDur, int dir){
  
  return (inputChargeT[2] >= chargeDur && dirMult * dir == 1) || (inputChargeT[3] >= chargeDur && dirMult * dir == -1);
}
  
  public void facingCheckAndChange(Fighter Opp){
    if(CurAction == Standing || CurAction == Crouching || CurAction == bWalk || CurAction == fWalk){
    if(facing == RIGHT && Opp.x < x ){
      changeAction(Turning);
      //dirMult = -1;
      //facing = LEFT;
      println("left");
          float l = AI_InputsScore[2];
    AI_InputsScore[2] = AI_InputsScore[3];
    AI_InputsScore[3] = l;
      
    }
    else if(facing == LEFT && Opp.x > x ){
      changeAction(Turning);
      //dirMult = 1;
      //facing = RIGHT;
      println("right");
                float l = AI_InputsScore[2];
    AI_InputsScore[2] = AI_InputsScore[3];
    AI_InputsScore[3] = l;
    }
    }

  }
  
  public void manualFacing(){
        if((CurAction == Standing || CurAction == Crouching || CurAction == fWalk || CurAction == bWalk) && (CollisionBox.bottom || y == GROUNDHEIGHT)){
          if(dirMult == 1 && inputs[3]){
            changeAction(Turning);
            //dirMult = -1;
            //facing = LEFT;
          }
          if(dirMult == -1 && inputs[2]){
            changeAction(Turning);
            //dirMult = 1;
            //facing = RIGHT;
          }
        }
  }
  
  public void extraEffOnHit(){
  }
  
  public Action operationsOnHit(Fighter Opp, ColCircle[] l_Boxes){
    Action resultAction = Opp.CurAction;
    
    if(l_Boxes != null){
    ColCircle HitBox = l_Boxes[0], c = l_Boxes[1];
    
      boolean instantBlock = false;//instant Block
        if((Opp.inputChargeT[3] < 6 && Opp.dirMult == 1) || (Opp.inputChargeT[2] < 6 && Opp.dirMult == -1)) instantBlock = true;
      boolean FD = false;//faultless Defense
        if( Opp.inputs[8] && Opp.curSuper > 0){ 
          VisEffectsList.add(new VisualEffect(Opp.x, Opp.y - 100, BurstEff, Opp.dirMult)); FD = true;
        }

     if( checkBlock(Opp, CurAction.attKind) && ( (!(new Grounded().cond(this, Opp) && new InAir().cond(Opp, this)) || FD ) )
        ){       
          //Opp.CurAction.reset();
          resultAction = Opp.Blocking;
          
          
          if(Opp.inputs[1] && !new InAir().cond(Opp, this)){
            resultAction.AttAnim = Opp.LBlock;
            resultAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            resultAction.AttAnim = Opp.HBlock;
            resultAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }
          
          if(CurAction.firstHit){
            Opp.Force.x = HitBox.forcex*dirMult + (CurAction.damage/10 + comboCount) * dirMult;
            Opp.Blocking.whenToUpdBoxs[0] = CurAction.affBlockStunT;
            

            if(instantBlock && FD){Opp.Blocking.whenToUpdBoxs[0] = CurAction.affBlockStunT-12;
               VisEffectsList.add(new PopUpMssg(Opp.x, Opp.y-100, "instant FD:"+Opp.inputChargeT[2]+":"+Opp.inputChargeT[3], 20, 0));
            }
            else if(instantBlock){Opp.Blocking.whenToUpdBoxs[0] = CurAction.affBlockStunT-8;
               VisEffectsList.add(new PopUpMssg(Opp.x, Opp.y-100, "instant Block:"+Opp.inputChargeT[2]+":"+Opp.inputChargeT[3], 20, 0));
            }
            
            Opp.throwInvu = CurAction.affBlockStunT+5;
            
                      if( new Grounded().cond(this, Opp) ) Force.x =  (CurAction.damage/2) * -dirMult; // changed to prevent certain attacks to catapult to other end of screen
            Force.x += (abs(HitBox.forcex)*2 + CurAction.damage/4) * CurAction.pushBackMult * -dirMult;
            
            if(FD){ Force.x += (abs(HitBox.forcex)*2 + CurAction.damage*2) * -dirMult; Opp.curSuper -= CurAction.damage*4; 
              if(!instantBlock)Opp.Force.x = HitBox.forcex*dirMult + (CurAction.damage/4 + comboCount) * dirMult;
            }
            else if(instantBlock) Force.x += (abs(HitBox.forcex)*2 + CurAction.damage/2) * -dirMult;          
            else Opp.curHP -= CurAction.damage/8;
            
            CurAction.specialEffectOnHit(this, Opp);
            VisEffectsList.add(new VisualEffect(Opp.x + c.addx + c.br/2 * Opp.dirMult, y + HitBox.addy, BlockEff, CurAction.damage/4));
                Soundeffects[4].cue(0);
                Soundeffects[4].play();
                cancelWindow = 6;
              
          }
          
          if(!CurAction.multiHit){
            CurAction.firstHit = false;
          }
          //frameFreeze = CurAction.damage/6;
          slowMoDur = 9; slowMoValue = 3;
  }
  
    else
      if(CurAction != BeingGrapped){
        //counter/punish code hinzufügen
        for(int  i = Opp.CurAction.curCollumn; i < Opp.CurAction.HitBoxCollect.size(); i++){
          if(Opp.CurAction.HitBoxCollect.get(i).size() > 0){
            Opp.counterState = true;
            break;
          }
        }
        
        Opp.Force.x = HitBox.forcex*dirMult + (CurAction.damage/4 + comboCount) * dirMult;
        Opp.Force.y = HitBox.forcey;
        Opp.HitStunT = CurAction.affHitStunT / (comboCount/2+1);
        Opp.gforce = 0;
          

        //multihitmoves dont work anymore , only last hit puts into stun
        if(CurAction.firstHit){
          
          
                  if( ( HitBox.forcey < 0 || Opp.y+1 < GROUNDHEIGHT || !new Grounded().cond(Opp, Opp) )){
          resultAction = Opp.softKD;
          Opp.softKD.whenToUpdBoxs[0] = CurAction.affHitStunT;
        }
        
        else if(CurAction.knocksDown && Opp.CurAction != Opp.Knockdown && Opp.CurAction != Opp.KDstandup){
          resultAction = Opp.Knockdown;
          Opp.Knockdown.whenToUpdBoxs[0] = CurAction.affHitStunT;
        }
        
        else
        {
          resultAction = Opp.InHitStun;
          Opp.InHitStun.whenToUpdBoxs[0] = CurAction.affHitStunT;
          if(Opp.inputs[1] && !new InAir().cond(Opp, this)){
            resultAction.AttAnim = Opp.LHit;
            resultAction.HurtBoxCollect.set(0, Opp.Crouching.HurtBoxCollect.get(0));
          }
          else{
            resultAction.AttAnim = Opp.HHit;
            resultAction.HurtBoxCollect.set(0, Opp.Standing.HurtBoxCollect.get(0));
          }
        }
        Opp.throwInvu = CurAction.affHitStunT + 5;
        
        //handling pushback and such
          
          if( new Grounded().cond(this, Opp) ) Force.x +=  (CurAction.damage/2 + comboCount) * CurAction.pushBackMult * -dirMult; // changed to prevent certain attacks to catapult to other end of screen
   
          Opp.curHP -= CurAction.damage / (comboCount/2+1);
          if(CurAction.superNeed == 0)curSuper += CurAction.damage*8;
          Opp.curSuper += CurAction.damage*4 * comboCount;
          
          CurAction.specialEffectOnHit(this, Opp);
             comboCount++;
             cancelWindow = 6;
             extraEffOnHit();
          VisEffectsList.add(new VisualEffect(Opp.x + c.addx + c.br/2 * Opp.dirMult, y + HitBox.addy, HitEff, CurAction.damage/4));
                Soundeffects[0].cue(0);
                Soundeffects[0].play();
                
         for(ActEffect a : CurAction.ActEffs){
            if(a.whereUsed == 0 && a.cond(this, Opp)){
              a.Effect(this, Opp);
            }}    
        }
        
         for(ActEffect a : CurAction.ActEffs){
            if( a instanceof CounterEff  && a.cond(this, Opp) && !hitstates[a.whereUsed]){
              a.Effect(this, Opp);
              Action[] Acts = {Opp.WallStick, Opp.WallBounce, Opp.Juggle, Opp.GroundBounce, Opp.Stagger};
              resultAction = Acts[a.whereUsed]; hitstates[a.whereUsed] = true;
            }
          }
         
         for(ActEffect a : CurAction.ActEffs){
            if(a.whereUsed == 2 && a.cond(this, Opp)){
              a.Effect(this, Opp);
         }}  
               
        if(!CurAction.multiHit){
          CurAction.firstHit = false;
        }
        //frameFreeze = CurAction.damage/6;
        slowMoDur = 9; slowMoValue = 3;
      }
    }
    return resultAction;
  }
  
  public ColCircle[] checkHit(Fighter Opp){//0,     1,                2,             3,          4,             5,              6,          7,
    Action[] OppStunStates = {Opp.InHitStun, Opp.BeingGrapped, Opp.Knockdown, Opp.softKD, Opp.WallStick, Opp.WallBounce, Opp.GroundBounce, Opp.Juggle, Opp.Stagger};
    boolean checkST = false; for(Action a : OppStunStates){ 
      if(a == Opp.CurAction){
             checkST = true;}
    }
    if(!checkST){comboCount = 0; Opp.counterState = false; Opp.punishState = false;
      for(int i = 0; i < hitstates.length; i++){hitstates[i] = false;}
    }
     
    ColCircle[] l_Boxes = null;

    for(ColCircle HitBox : HitBoxes){
    for(ColCircle c : Opp.HurtBoxes){
      ColRect OppCb = Opp.CollisionBox; ColCircle l_cGrabBox = new ColCircle(0, 0, OppCb.br, OppCb.ho); l_cGrabBox.setxy(Opp.x, Opp.y-l_cGrabBox.br/2);
      if(CurAction.attKind == Action.GRAB && HitBox.compare(l_cGrabBox, dirMult, Opp.dirMult)){  //Opp Strike check hinzufügen -> throwinvu auf 5 reduzieren
        if(Opp.throwInvu > 0){break;}       
        l_Boxes = new ColCircle[2];
        l_Boxes[0] = HitBox;
        l_Boxes[1] = l_cGrabBox;
          break;
        
      }
      else
      if(CurAction.attKind != Action.GRAB && HitBox.compare(c, dirMult, Opp.dirMult)){
        l_Boxes = new ColCircle[2];
        l_Boxes[0] = HitBox;
        l_Boxes[1] = c;
          
          break;
        }
        
        
      }
    }
 
    //Projektile 
        for(int indexProj = Projectiles.size()-1; indexProj >= 0; indexProj--){
          Projectile p = Projectiles.get(indexProj);
    for(ColCircle c : Opp.HurtBoxes){
      if(p.hitOpp && p.HitBox.compare(c, 1, 1)){  
                if( checkBlock(Opp, p.attKind) 
                ){
          Opp.CurAction.reset();
          //Opp.CurAction = Opp.Blocking;
          Opp.changeAction(Opp.Blocking);
          Opp.Force.x += p.forcex / 4;
          Opp.Force.y += p.forcey / 4;
          Opp.Blocking.whenToUpdBoxs[0] = p.hitStun/2;
          Opp.curHP -= CurAction.damage/10;
           VisEffectsList.add(new VisualEffect(p.x, p.y, BlockEff, p.damage/4));
                Soundeffects[0].cue(0);
                Soundeffects[0].play();
                
                    if(Opp.inputs[1] && !new InAir().cond(Opp, this)){
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
          Opp.softKD.whenToUpdBoxs[0] = p.hitStun;
        }
        else{
          Opp.CurAction.reset();
          Opp.InHitStun.whenToUpdBoxs[0] = p.hitStun;
          //Opp.CurAction = Opp.InHitStun;
          Opp.changeAction(Opp.InHitStun);
          if(Opp.inputs[1] && !new InAir().cond(Opp, this)){
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
        ( (Opp.CurAction == Opp.Standing || Opp.CurAction == Opp.Falling || Opp.CurAction == Opp.bWalk)
        && (attKind == Action.HIGH || attKind == Action.AIR || attKind == Action.MID || attKind == Action.NOTHING))
        || 
        ( (Opp.CurAction == Opp.Crouching || (Opp.CurAction == Opp.bWalk && Opp.inputs[1]) ) 
        && (attKind == Action.LOW || attKind == Action.MID || attKind == Action.NOTHING))
        )
        
        ||
        
        (Opp.CurAction == Opp.Blocking 
        && (
        (attKind == Action.MID || attKind == Action.NOTHING)
        ||
        ((!Opp.inputs[1] || new InAir().cond(Opp, this)) && (attKind == Action.HIGH || attKind == Action.AIR) )
        || 
        (Opp.inputs[1] && attKind == Action.LOW )
        ) 
        );
  }
  
  public void updateProjectiles(Fighter Opp){
   for(int i = Projectiles.size()-1; i >= 0; i--){
      Projectile p = Projectiles.get(i);
      p.gameLogic(this, Opp);
      
      if(Projectiles.get(i).exTimer == 0){
               if(p.destrEff != null){
                 println("projdie");
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
    HurtBoxes.clear();
    HitBoxes.clear(); 
    CurAction.reset();
    CurAction = a; a.reset();
    a.clearAndCurBoxes(this);
    if(a.AttAnim != null){
      CurAnimation = a.AttAnim;
      CurAnimation.timer = 0; CurAnimation.curCollumn = 0;
    }
    else{ CurAnimation = null;}
  }
  
    public void AIControll2(Fighter Opp){
    float aggroLevel = dist(x, y, Opp.x, Opp.y)*0.1f + (Opp.maxHP/(Opp.curHP+1)) - (curHP/(maxHP+1));
    float attChance, moveChance = dist(x, y, Opp.x, Opp.y);
    //Attproperties needed
    //int condAttKind = Action.MID;
    //int condAttTime;
    
    if(whichDirHold(Opp, -1) || Opp.CurAction == Opp.Blocking ){  //Check Enemy Blocking
      if(Opp.inputs[1]){
        //condAttKind = Action.HIGH;
        inputs[1] = false;
        AI_InputsScore[0] += 0.1f;
      }
      else{ //condAttKind = Action.LOW; 
         inputs[1] = true; AI_InputsScore[1] += 0.1f;}
      
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
    
       
   // when in Hitstun
   if(CurAction == InHitStun){
     inputs[horDir(-1)] = true;
     AI_InputsScore[horDir(-1)] += 0.1f;
     if(Opp.CurAction.attKind == Action.LOW){
       inputs[1] = true;
       AI_InputsScore[1] += 0.1f;
     }
     else if(Opp.CurAction.attKind == Action.HIGH || Opp.CurAction.attKind == Action.AIR){
              inputs[1] = false;
       AI_InputsScore[1] -= 0.1f;
     }
   }
   
   
   
     
  }
  
  
  // "0000 (Dir) 000 (AttButs) 0 (RC) : timeactive"
  
  InputRecord FBlightR = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0110 000 0 : 2",
  "0010 000 0 : 2",
  "0010 100 0 : 2",
  "0010 100 0 : 2"
  );
  InputRecord DPhR = new InputRecord(this, 2, true,
  "0010 000 0 : 2",
  "010 000 0 : 2",
  "0110 000 0 : 2",
  "0010 010 0 : 2",
  "0010 010 0 : 2"
  );
  InputRecord rFBh = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0101 000 0 : 2",
  "0001 000 0 : 2",
  "0001 010 0 : 2",
  "0001 010 0 : 2"
  );
  InputRecord rFBl = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0101 000 0 : 2",
  "0001 000 0 : 2",
  "0001 100 0 : 2",
  "0001 100 0 : 2"
  );
  InputRecord Doublf = new InputRecord(this, 2, true,
  "0010 000 0 : 2",
  "000 000 0 : 1",
  "0010 000 0 : 2",
  "0010 000 0 : 2"
  );

  
  public void AIControll3(Fighter Opp){
    float aggro = 0;
    try{
    aggro = dist(x, y, Opp.x, Opp.y)*0.1f + (Opp.maxHP/(Opp.curHP+1)) - (curHP/(maxHP+1));
    }catch(ArithmeticException e){
      aggro = 100;
    }
    float distxy = dist(x, y, Opp.x, Opp.y), distx = dist(x, 0, Opp.x, 0), disty = dist(0, y, 0, Opp.y);
    
    if(distx <320 && disty > 50 && aggro >= 70 && new InAir().cond(this, Opp)){
      Recorder = rFBl;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx <350 && disty > 150 && aggro >= 30){
      Recorder = DPhR;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx <370 && disty > 220 && aggro >= 40){
      Recorder = rFBh;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx > 660 && aggro >= 60){
      Recorder = Doublf;    
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }
    else
    if(distx > 500 && aggro >= 20){
      Recorder = FBlightR;    
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }
    else Recorder = null;

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
             AI_InputsScore[8] += 0.1f;
           }
    
   String data = "";
    for(int i = 0; i < AI_InputsScore.length; i++){
      if( random(0, 1) < AI_InputsScore[i] ){
        inputs[i] = true;
      }
      else { inputs[i] = false; }
      
      data += AI_InputsScore[i] + ":";
    }
    //println(data);
    
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
    
    if(firstPressInp[0] && AI_InputsScore[0] > 0.2f){
      AI_InputsScore[0] -= 0.1f;
    }
    
    abstand = dist(x, y, Opp.x, Opp.y);
    pHP = curHP;
    pOppHP = Opp.curHP;
  }
  
  public void drawBars(int mult){
    float centerZ2 = 0 / tan(PI*30.0f / 180.0f);
    drawBar( PApplet.parseInt(Camerabox.x) - mult*(initWidth/4), 20 - PApplet.parseInt(0) , centerZ2, maxHP, curHP, mult * -180);
    drawBar( PApplet.parseInt(Camerabox.x) - mult*(initWidth/4), GROUNDHEIGHT+10 - PApplet.parseInt(0) , centerZ2, maxSuper, curSuper, mult * -140);
  } 
  
}//Ende Hauptklasse

class F_OBHaus extends Fighter{
  F_OBStand Stand; 
  
  
  class F_OBStand extends Fighter{
    F_OBHaus Master; int sumnTimer = 0; final int NOTSUMMONED = 0, SUMMONED = 1, PUPPET = 2; int State = NOTSUMMONED;
    Action Summon, Desummon, Sp1, Sp2Rush, j_downHeavy;
    
   F_OBStand(int x, int y, char[] charinputs, ControlDevice device, F_OBHaus Master){
    super(x,  y, charinputs, device); this.Master = Master;
  }
   F_OBStand(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
  }
  
  public void specSetup(){
      name = "OstblockHouse";
   CollisionBox = new ColRect(0, 0, 150, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
      int[] times0 = {5};
      Animation Anim0 = new Animation(times0, 0, 1, "OBHouse/OBS-st/OBS-st");
      Standing = new Action("FH-st-idle", Anim0, false); Standing.gravityActive = true; Standing.addingForce = false;
      Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -240, 280, 180, 0, 0, -1));
          
        int[] times2 = {5};
  Crouching = new Action("fh-cr", new Animation(times2, 0, 1, "OBHouse/OBS-st/OBS-st"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
      
          int[] times14 = {4, 4, 4, 4, 4, 4, 4};
  Condition[] Cond1 = {new fPButCond(5)};    
  Animation Anim14 = new Animation(times14, 100, 7, "OBHouse/OBS-sp1/OBS-1sp");
  LightNormal = new Action("OBS-light", Anim14, Cond1, 20, 6, 12, Action.MID, 1, true, false, false, false);
  
  cr_HeavyNormal = new Action("OBS-cr-heay", 
  new Animation(new int[]{4, 4, 4, 2, 6, 6, 4, 4, 4}, 0, 9, "OBHouse/OBS-cr-heay/OB-cr-heavy"), 
  Cond1, 30, 14, 16, Action.MID, 1, true, false, false, false);
  cr_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.JUGGLE)};
  
  Action HeavyNormal_p2 = new Action("OBS-st-fh2", 
  new Animation(new int[]{5, 4, 4, 4, 4, 5, 2, 4, 2}, 0, 9, "OBHouse/OBS-st-fh/p2/OB-st-fh"), 
  Cond1, 10, 5, 4, Action.HIGH, 1, true, false, false, false);
  
  HeavyNormal = new Action("OBS-st-fh", 
  new Animation(new int[]{2, 2, 2, 2, 5, 2, 8, 2, 100}, 0, 9, "OBHouse/OBS-st-fh/OB-st-fh"), 
  Cond1, 20, 5, 12, Action.HIGH, 1, true, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{new ChangeActTo(HeavyNormal_p2, 1, new Grounded(), new ActTimeCond(3, 1, 3, 100) )};
  HeavyNormal.AttAnim.loop = false;
  
  j_HeavyNormal = new Action("OBS-j-heay", new Animation(new int[]{2, 2, 3, 2,    6, 4, 2, 2}, 100, 8, "OBHouse/OBS-j-heay/OBS-j-heay"), 
  Cond1, 24, 8, 10, Action.HIGH, 1, false, false, false, false);
  j_HeavyNormal.ActEffs = new ActEffect[]{new OBS_setSFToM(Master, 1, new ActTimeCond2(0, 0) )};
  
  j_downHeavy = new Action("OBS-j-dh", new Animation(new int[]{2, 2, 6, 2,    4, 3, 3, 8}, 40, 80, 8, "OBHouse/OBS-j-dH/OBS-j-dH"), 
  new Condition[]{new fPButCond(5), new ButCond(1)}, 18, 8, 10, Action.HIGH, 1, false, false, false, false);
  j_downHeavy.ActEffs = new ActEffect[]{new CounterEff(Action.GROUNDBOUNCE)};
  
    Sp1 = new Action("OBS-sp1", 
  new Animation(new int[]{4, 2, 4, 4, 2, 2, 2, 4, 8, 2, 2, 2, 4, 2}, 280, 14, "OBHouse/OBS-special1snake/OBS-1sp"), 
  Cond1, 10, 5, 4, Action.MID, 1, false, false, false, false);
  Sp1.ActEffs = new ActEffect[]{new changeToOwnXY(620, 0, new ActTimeCond2(4, 8) ) };
  Sp1.AttAnim.loop = false;
     
  Sp2Rush = new Action("OBS-sprush", 
  new Animation(new int[]{2, 4, 2, 4, 4, 4, 4, 4, 4, 2, 2, 2}, 80, 12, "OBHouse/OBS-revRush/OBS-revRush"), 
  Cond1, 20, 11, 4, Action.MID, 1, false, false, false, true);
  //Sp2Rush.ActEffs = new ActEffect[]{new changeToOwnXY(620, 0, new ActTimeCond2(4, 8) ) };
  //Sp2Rush.AttAnim.loop = false;
     
  Summon = new Action("OBS-summon", new Animation(new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2}, 0, 9, "OBHouse/OBS-summon/OBS-summon"), 
new Condition[]{}, 
0, 0, 0, Action.MID, 100, true, false, false, false);

  Desummon = new Action("OBS-desummon", new Animation(new int[]{4, 3, 3, 4, 4}, 0, 5, "OBHouse/OBS-desummon/OBS-desummon"), 
new Condition[]{}, 
0, 0, 0, Action.MID, 100, true, false, false, false);
Desummon.ActEffs = new ActEffect[]{new OBS_setState(Master, NOTSUMMONED, 1, new ActTimeCond2(0, 18) )};

  InHitStun.AttAnim = new Animation(new int[]{2}, 0, 1, "OBHouse/OBS-hitStun/OBS-hitStun");
      
            Action[][] ActTab = { {}, {}, {}, {} };

  fillActionsList(ActTab);
  }
  
 /* void standingStateReturn(Fighter Opp){
    CurAction.playAction2(this, Opp, Standing);
  }*/
  
  public void normalWalk(){} public void jump(){} public void dash(){}
  public void st_Normals(){}
  //void st_Normals(){  Force.x += Master.Force.x; Force.y += Master.Force.y; x += Force.x; y += Force.y;}
  //void j_Normals(){  Force.x += Master.Force.x; Force.y += Master.Force.y; x += Force.x; y += Force.y;}
  
  public void faultlessDefense(){}
  
  public void fighterActionsExtra(){ }
   
  public void standingStateReturn(Fighter Opp){
    if(y == GROUNDHEIGHT || CollisionBox.bottom) curAirActions = maxAirActions;
    
        CurAction.playAction2(this, Opp, Standing);
      
  }
   
   public void facingCheckAndChange(Fighter Opp){
    if(CurAction == Standing || CurAction == Crouching || CurAction == bWalk || CurAction == fWalk){
    if(facing == RIGHT && Opp.x < x ){
      dirMult = -1;
      facing = LEFT;     
    }
    else if(facing == LEFT && Opp.x > x ){
      dirMult = 1;
      facing = RIGHT;
    }
    }
  }
   
   public void gameLogic(Fighter Opp){ 
     facingCheckAndChange(Opp);
     if(State == SUMMONED && CurAction == Standing && sumnTimer < 40) sumnTimer++;
     else if(State == SUMMONED && CurAction != Standing) sumnTimer = 0;
     if(State == SUMMONED && CurAction == Standing && sumnTimer >= 40) changeAction(Desummon);
     
     device = Master.device; if(device != null) setConDevice(); 
     PlContr = Master.PlContr; if(PlContr.device != null) PlContr.setConDevice();
    if(State == NOTSUMMONED){ changeAction(Standing); facingCheckAndChange(Opp); x = Master.x; y = Master.y; return;}
    if(State == SUMMONED){x += Force.x; y += Force.y;//x = Master.x; y = Master.y;
    }
    super.gameLogic(Opp);
    facingCheckAndChange(Opp);
    hitOnStand(Opp);
  }
  
  public void extraEffOnHit(){
    Master.comboCount++;
  }
  
 private void hitOnStand(Fighter Opp){
   if(State == NOTSUMMONED)return;
    for(ColCircle OppHitbox : Opp.HitBoxes){
      for(ColCircle StandHurtbox : this.HurtBoxes){
        if(StandHurtbox.compare(OppHitbox, this.dirMult, Opp.dirMult) ){
          /*f(State == SUMMONED){
            InHitStun.whenToUpdBoxs[0] = Opp.CurAction.affHitStunT; 
            changeAction(InHitStun);
            Master.InHitStun.whenToUpdBoxs[0] = Opp.CurAction.affHitStunT; 
            Master.changeAction(Master.InHitStun);
          }
          else */
          if(State == SUMMONED){
            changeAction(Desummon);
            Master.InHitStun.whenToUpdBoxs[0] = Opp.CurAction.affHitStunT; 
            Master.changeAction(Master.InHitStun);
          }
          break;
        }
        
      }
    }
    
    if(Opp instanceof F_OBHaus) {F_OBStand OppStand = ( (F_OBHaus) Opp).Stand;
    if(OppStand.State == NOTSUMMONED || State == NOTSUMMONED)return;
    //Stand-Stand HitCheck
    for(ColCircle StandHitbox : this.HitBoxes){
      for(ColCircle OStandHurtbox : OppStand.HurtBoxes){
        
       if(StandHitbox.compare(OStandHurtbox, this.dirMult, Opp.dirMult) ){
         /*if(OppStand.State == SUMMONED){
          OppStand.InHitStun.whenToUpdBoxs[0] = this.CurAction.affHitStunT; 
          OppStand.changeAction(OppStand.InHitStun);
         }else 
         if(OppStand.State == SUMMONED){
           OppStand.changeAction(OppStand.Desummon);
         }*/
         
          Opp.InHitStun.whenToUpdBoxs[0] = this.CurAction.affHitStunT; 
          Opp.changeAction(Opp.InHitStun);
          break;
        }
        
      }
    }
    
    }
  
  }
  
  
  }//Ende der inneren Standklasse


  Action FarMidNormal, st_fHeavy, j_MidNormal2, j_dH;
  Action Special1, Sp2Snake, Sp3dive, Sp4train, Sp5Rush, Summon;
  
 F_OBHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_OBHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
 F_OBHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
 F_OBHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
 
 F_OBHaus(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
  }
 
 public ColCircle[] checkHit(Fighter Opp){
   ColCircle[] ToReturn = super.checkHit(Opp), ToReturn2 = Stand.checkHit(Opp);
   if(Stand.State != Stand.NOTSUMMONED && ToReturn2 != null){ return ToReturn2;} 
   else
   if(ToReturn != null){ return ToReturn;}
      
    return null;
  }
  
  public Action operationsOnHit(Fighter Opp, ColCircle[] l_Boxes){
    Action resultAction = Opp.CurAction;
    ColCircle[] ToReturn = super.checkHit(Opp), ToReturn2 = Stand.checkHit(Opp);
    if(Stand.State != Stand.NOTSUMMONED && ToReturn2 != null){return Stand.operationsOnHit(Opp, l_Boxes);} 
     else
    if(ToReturn != null){return super.operationsOnHit(Opp, l_Boxes);}
    return resultAction;
  }
 
 public void gameLogic(Fighter Opp){
   //if(Stand.State == Stand.NOTSUMMONED){Stand.changeAction(Stand.Standing); Stand.facingCheckAndChange(Opp); } //xyPos anpassen, nicht nur state
   Stand.gameLogic(Opp);
   super.gameLogic(Opp);
 }
 
 public void draw(){
   if(Stand.State != Stand.NOTSUMMONED)Stand.draw();
   super.draw();
 }

 public void fighterActionsExtra(){
 }
 
  public void keyPressed(){Stand.keyPressed(); super.keyPressed();}
  public void keyReleased(){Stand.keyReleased(); super.keyReleased();}
  
public void specSetup(){
  Stand = new F_OBStand( (int) x, (int) y, charinputs, device, this);
  Stand.setup();
  name = "OstblockHouse";
   CollisionBox = new ColRect(0, 0, 120, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "OBHouse/OB-st/OB-st");
      Standing = new Action("FH-st-idle", Anim0, false);
      
  Falling.AttAnim = new Animation(new int[]{4}, 0, 1, "OBHouse/OB-jumpfall/OB-jfall");
      
    Summon = new Action("FH-st-idle", Anim0, 
new Condition[]{new OBSH_checkState(this, Stand.NOTSUMMONED, true), new fPButCond(4), new comfPButC(new int[][]{{2, 1, 4}})}, 
0, 0, 0, Action.MID, 100, true, false, false, false);
Summon.ActEffs = new ActEffect[]{new OBS_setStateAndAct(this, Stand.Standing, Stand.NOTSUMMONED, 1)};
  
        
  int[] times1 = {8, 8, 8, 8, 8, 8, 8, 8, 8};
  Animation Anim1 = new Animation(times1, 0, 9, "OBHouse/OB-fwalk/OB-fwalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 3, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2, 0);
  
  int[] times3 = {1, 4, 1, 2, 2};
  Animation Anim3 = new Animation(times3, 0, 5, "OBHouse/OB-jump/OB-jump");
  Anim3.loop = false;
  Jumping = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  Jumping.resetAnim = true;

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  Jumping.addAllLists(1, 2, 0, -20);
  
  fDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  fDiaJump.resetAnim = true;

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
      fDiaJump.addAllLists(1, 2, 11, -20);
  
  bDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  bDiaJump.resetAnim = true;

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bDiaJump.addAllLists(1, 2, -7.5f, -20);
  
  Jumping2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  
  FDash = new Action("OB-fdash", new Animation(new int[]{6, 2, 8, 2, 8}, 0, 5, "OBHouse/OB-fdash/OB-fdash"),
  0, 0, 0, 0, false, false, false, false);
  FDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
    
  BDash = new Action("OB-bdash" ,new Animation(new int[]{4, 2, 10, 2, 5}, 0, 5, "OBHouse/OB-bdash/OB-bdash"),
  0, 0, 0, 0, false, false, false, false);
  BDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
  
  airFDash = new Action("OB-afdash", new Animation(new int[]{6, 2, 8, 2, 8}, 0, 5, "OBHouse/OB-fdash/OB-fdash"),
  0, 0, 0, 0, false, false, false, false);
    airFDash.allButSelfCancel = true;

  airBDash = new Action("OB-abdash", new Animation(new int[]{4, 3, 11, 4, 3}, 0, 5, "OBHouse/OB-bdash/OB-bdash"),
  0, 0, 0, 0, false, false, false, false);
  //airBDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
  
    int[] times2 = {5};
  Crouching = new Action("fh-cr", new Animation(times2, 0, 1, "OBHouse/OB-cr/OB-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "OBHouse/OB-stun/OB-hitStunH"); LHit = new Animation(times1, 0, 1, "OBHouse/OB-stun/OB-hitStunL");
  HBlock = new Animation(times1, 0, 1, "OBHouse/OB-stun/OB-blockStunH"); LBlock = new Animation(times1, 0, 1, "OBHouse/OB-stun/OB-blockStunL");
  
  int[] ani9 = {6};
  Blocking = new Action(ani9);
  Blocking.updFrameDataArr(0, 2); 
  Blocking.updFrameDataArr_float(0, 0, 0);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 200, 160));
  
    int[] times14 = {2, 2, 5, 2};
  Condition[] Cond1 = {new fPButCond(4)};    
  Animation Anim14 = new Animation(times14, 0, 4, "OBHouse/OB-st-light/OB-st-light");
  LightNormal = new Action("OBH-st-light", Anim14, Cond1, 12, 5, 4, Action.MID, 1, true, false, false, false);
  //LightNormal.selfCancel = true;
  
  Condition[] Cond2 = {new fPButCond(6), new xDistOpp(160, true)};
    int[] times16 = {5, 2, 8, 5, 4};
    Animation Anim16 = new Animation(times16, 50, 5,"OBHouse/OB-st-clmed/OB-st-clmed");
  MidNormal2 = new Action("OBH-st-clmed", Anim16, Cond2, 20, 12, 12, 3, 3, true, false, false, false);
  MidNormal2.ActEffs = new ActEffect[]{new GatlingEff(LightNormal), new SnglGatEff(FarMidNormal, new fPButCond(6)),
  new GatlingEff(new Condition[]{new ButCond(1)}, cr_HeavyNormal)};

  FarMidNormal = new Action("OBH-st-fmed", 
  new Animation(new int[]{2, 7, 3, 3, 3, 4, 4}, 50, 7,"OBHouse/OB-st-fmed/OB-st-fmed"), 
  new Condition[]{new fPButCond(6), new xDistOpp(160, false)}, 24, 12, 12, 3, 4, true, false, false, false);
  
  Condition[] Cond3 = {new fPButCond(5)};
    int[] times17 = {5, 2, 2, 10, 4, 4};
    Animation Anim17 = new Animation(times17, 0, 6,"OBHouse/OB-st-summon/OB-st-summon");
  HeavyNormal = new Action("OBH-st-summon", Anim17, Cond3, 16, 12, 18, Action.MID, 5, true, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{ new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)), 
  new OBS_setAction(this, Stand.LightNormal, 1, new ActTimeCond2(2, 1), new OBSH_checkAction(this, true, Stand.Summon, Stand.Standing, Stand.Crouching) )};
  
  int[] times18 = {4, 2, 5, 4};
  Animation Anim18 = new Animation(times18, 25, 4, "OBHouse/OB-cr-light/OB-cr-light");
  cr_LightNormal = new Action("OB-cr-light", Anim18, Cond1, 15, 5, 3, 3, 2, true, false, false, false);
  cr_LightNormal.ActEffs = new ActEffect[]{ new SoundEff("fh-cr-l", 0, 0) };

  int[] times19 = {4, 6, 2, 6, 3, 3, 4, 2};
  Animation Anim19 = new Animation(times19, 50, 8, "OBHouse/OB-cr-med/OB-cr-med");
  cr_MidNormal = new Action("OB-cr-med", Anim19, new Condition[]{new ButCond(6)}, 26, 10, 10, Action.LOW, 4, true, false, true, false);
       
  cr_HeavyNormal = new Action("OBH-st-summon", Anim17, Cond3, 16, 12, 18, Action.MID, 5, true, false, false, false);
  cr_HeavyNormal.ActEffs = new ActEffect[]{new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),  
    new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(1, 1), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.cr_HeavyNormal, 1, new ActTimeCond2(4, 1), new OBSH_checkAction(this, true, Stand.Crouching, Stand.Standing, Stand.Summon) )};
  
  st_fHeavy = new Action("OBH-st-summon", Anim17, 
  new Condition[]{new facingCond(1), new fPButCond(5)}, 
  16, 12, 18, Action.MID, 5, true, false, false, false);
  st_fHeavy.ActEffs = new ActEffect[]{new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)), 
    new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(1, 1), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.HeavyNormal, 1, new ActTimeCond2(4, 1), new OBSH_checkAction(this, true, Stand.Standing, Stand.Crouching, Stand.Summon) ) };
  
  j_HeavyNormal = new Action("OBH-st-summon", Anim17, Cond3, 16, 12, 18, Action.MID, 5, false, false, false, false);
  j_HeavyNormal.ActEffs = new ActEffect[]{ //new OBS_setSFToM(this, 1, new ActTimeCond(0, 0), new OBSH_checkAction(this, Stand.Standing, true)),
    new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkAction(this, true, Stand.Standing)),//, new OBSH_checkState(this, Stand.NOTSUMMONED, true)),  
  new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.j_HeavyNormal, 1, new ActTimeCond2(1, 1), new OBSH_checkAction(this, true, Stand.Summon, Stand.Standing) ),
  new GravEff(0, 0, false), new GravEff(1, 4, true)};
  
    j_dH = new Action("OBH-st-summon", Anim17, new Condition[]{new fPButCond(5), new ButCond(1) }, 16, 12, 18, Action.MID, 5, false, false, false, false);
  j_dH.ActEffs = new ActEffect[]{ //new OBS_setSFToM(this, 1, new ActTimeCond(0, 0), new OBSH_checkAction(this, Stand.Standing, true)),
    new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true), new OBSH_checkAction(this, true, Stand.Standing, Stand.Crouching)),//, new OBSH_checkState(this, Stand.NOTSUMMONED, true)),  
  new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.j_downHeavy, 1, new ActTimeCond2(1, 1), new OBSH_checkAction(this, true, Stand.Summon, Stand.Standing, Stand.Crouching) ),
  new GravEff(0, 0, false), new GravEff(1, 4, true)};

  int[] times20 = {5, 2, 5, 3};
  Animation Anim20 = new Animation(times20, 20, 50, 4, "OBHouse/OB-j-light/OB-j-light");
  j_LightNormal = new Action("OB-j-l", Anim20, Cond1, 12, 4, 3, Action.AIR, 1, true, true, false, false);
  j_LightNormal.selfCancel = true;
  j_LightNormal.ActEffs = new ActEffect[]{new GatlingEff(j_MidNormal)};
  
  int[] times22 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  j_MidNormal = new extraForceOnHitAct( "OB-j-m", new Animation(times22, 20, 120, 10, "FHouse/FH-j.med/HF-j.med"),
                               16, 8, 10, Action.AIR, true, true, false, false, 3, -16);
            Condition[] Cond20 = {new ButCond(6), new ButCond(1)};
            j_MidNormal.Conds = Cond20; j_MidNormal.attWeight = 4;
  j_MidNormal.ActEffs = new ActEffect[]{new GatlingEff(j_LightNormal)};
                               
  int[] times28 = {2, 4, 2, 6, 2, 2};
  j_MidNormal2 = new Action( "OB-j-m", new Animation(times28, 20, 70, 6, "OBHouse/OB-j-med/OB-j-med"), new Condition[]{new ButCond(6)},
                               14, 8, 8, Action.AIR, 3, true, true, false, false);
  j_MidNormal2.ActEffs = new ActEffect[]{new GatlingEff(j_MidNormal)};
                             
  
  int[] times24 = {2, 2, 2, 2, 4, 2, 2, 2};
  int[][] DPvars = {{6, 2, 3}, {6, 3, 2, 3, 6}, {6, 3, 2, 3}, {6, 2, 6}};
  Condition[] Cond4 = {new comfPButC(DPvars), new fPButCond(5)};
  Special1 = new Action("FH-sp1", new Animation( times24, 50, 8, "FHouse/FH-sp1/FH-1sp"), Cond4, 18, 16, 6, Action.MID, 20, true, true, true, false);
  Special1.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, true), new ForceAddEff(8, 0, false), new ForceAddEff(12, 0, false)};
  
    
  Sp2Snake = new Action("OBH-st-summon", Anim17, 
  new Condition[]{new fPButCond(5), new comfPButC(new int[][]{ {2, 3, 6} } )}, 
  16, 12, 18, Action.MID, 7, true, false, false, false);
  Sp2Snake.ActEffs = new ActEffect[]{new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),  
    new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(1, 1), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.Sp1, 1, new ActTimeCond2(4, 1), new OBSH_checkAction(this, true, Stand.Standing, Stand.Summon) )};
  
    Action Sp3diveP3 = new Action("FH-j-light", new Animation(new int[]{2, 2, 2}, 0, 0, 3, "OBHouse/OB-gdive/p3/OB-gdive"), 
  new Condition[]{}, 
  12, 4, 3, Action.MID, 1, true, false, false, false);
  
    Animation Sp3neutral = new Animation(new int[]{2}, 0, 0, 1, "OBHouse/OB-gdive/p2/OB-gdive"),
  Sp3forward = new Animation(new int[]{2}, 0, 0, 1, "OBHouse/OB-gdive/p2/OB-gdivefor"), 
  Sp3back = new Animation(new int[]{2}, 0, 0, 1, "OBHouse/OB-gdive/p2/OB-gdiveback"); 
  Sp3neutral.loop = true; Sp3forward.loop = true; Sp3back.loop = true;
    Action Sp3diveP2 = new Action("OBH-sp2p2", Sp3forward, 
  new Condition[]{ new ButCond(6) }, 
  12, 4, 3, Action.MID, 1, true, false, false, false); Sp3diveP2.collision = false; Sp3diveP2.resetAnim = false;
  Sp3diveP2.ActEffs = new ActEffect[]{ new ChangeAnimTo(Sp3forward, 1, new facingCond(1)), new ChangeAnimTo(Sp3back, 1, new facingCond(-1)), new ChangeAnimTo(Sp3neutral, 1, new FalseCond(new facingCond(1), new facingCond(-1))),
  new SetForce(14, 0, 1, new facingCond(1)), new SetForce(-14, 0, 1, new facingCond(-1)), new ChangeActTo(Sp3diveP3, 1, new FalseCond(new ButCond(6)) ),
  new ChangeActTo(Sp3diveP3, 1, new ActTimeCond2(0, 79))};
  
  Sp3dive = new Action("FH-j-light", new Animation(new int[]{2, 2, 2, 2, 2}, 0, 0, 5, "OBHouse/OB-gdive/OB-gdive"), 
  new Condition[]{new fPButCond(6), new comfPButC(new int[][]{ {2, 1, 4} } )}, 
  12, 4, 3, Action.MID, 50, true, false, false, false);
  Sp3dive.ActEffs = new ActEffect[]{ new ChangeActTo(Sp3diveP2, 1, new ActTimeCond2(1, 2) ) };
  
   Projectile TrainProj = new Projectile(0.0f, 0.0f, 50, -10, 160, 60, 10.0f, 0.0f, 1, 120, 20, 10, 10, false, false, false, true, true) ;
   TrainProj.setAnims(new Animation(new int[]{10, 10, 10, 10}, 0, -50, 4, "Projectiles/Train/Train"), new Animation(new int[]{10, 10, 10, 10}, 0, -25, 4, "Projectiles/Train/Train"));
                  int[] times32 = {4, 4, 2, 6, 6,
                2, 6, 2, 6, 4};
         Condition[] Cond8 = {new fPButCond(4), new dirCombCond(new int[]{2,3,6})};
   Sp4train = new Action("OB-trainFB",
   new Animation( times32, 50, 10, "OBHouse/OB-trainFB/OB-trainFB"), Cond8, 32, 10, 4, Action.MID, 18, true, false, false, false);
   Sp4train.ActEffs = new ActEffect[]{ new ProjAddEff( TrainProj, 1, new ActTimeCond2(1, 1) )};
   
     Sp5Rush = new Action("OBH-st-summon", Anim17, 
  new Condition[]{new fPButCond(5), new comfPButC(new int[][]{ {2, 1, 4} } )}, 
  16, 12, 18, Action.MID, 7, true, false, false, false);
  Sp5Rush.ActEffs = new ActEffect[]{new OBS_changeXY(this, 0, 0, new ActTimeCond2(0, 0), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),  
    new OBS_setStateAndAct(this, Stand.Summon, Stand.SUMMONED, 1, new ActTimeCond2(1, 1), new OBSH_checkState(this, Stand.NOTSUMMONED, true)),
  new OBS_setAction(this, Stand.Sp2Rush, 1, new ActTimeCond2(4, 1), new OBSH_checkAction(this, true, Stand.Standing, Stand.Summon) )};
   
    Action[][] ActTab = {
  {st_fHeavy, LightNormal, FarMidNormal, MidNormal2, HeavyNormal, FDash, BDash, Jumping, fDiaJump, bDiaJump, Special1, Summon, Sp2Snake, Sp3dive, Sp4train, Sp5Rush, NormalThrow},
  {cr_LightNormal, cr_MidNormal, cr_HeavyNormal, Special1},
  {j_LightNormal,j_MidNormal2, j_MidNormal, j_dH, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2, NormalThrow, Special1, Sp2Snake, AirThrow},
  {Special1, Summon, Sp2Snake, Sp3dive, Sp4train, Sp5Rush}
};

  fillActionsList(ActTab);
   
}
  public void st_Normals(){
    
  }
  
  public void cr_Normals(){
    /*if(inputs[6] && Stand.State == Stand.NOTSUMMONED){ Stand.State = Stand.SUMMONED; Stand.changeAction(Stand.Summon);}
    else if(inputs[6] && Stand.State == Stand.SUMMONED) Stand.changeAction(Stand.Desummon);
    
    if(inputs[5] && Stand.State == Stand.SUMMONED ) Stand.State = Stand.PUPPET;
    else if(inputs[5] && Stand.State == Stand.PUPPET) Stand.State = Stand.SUMMONED;*/
  }
  
  public void j_Normals(){
    }   
         
}

//FAMILIENHAUS###############################################################################################################################################################################################
class F_FHaus extends Fighter{

  Action st_fHeavy, j_MidNormal2;
  Action BaseNormal, RoofNormal;
  ProjAction Fireball, Fireball2;
  Action Special1, Special3, Special4, Super1;
  Animation FB1AnimUp, FB1AnimMid, FB1AnimDown, BettDestrEff;
  
  /*  Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, st_fHeavy, BaseNormal, RoofNormal, FDash, BDash, Jumping, fDiaJump, bDiaJump, Super1},
  {cr_LightNormal, cr_MidNormal, cr_HeavyNormal, Super1},
  {j_LightNormal, j_MidNormal, j_MidNormal2, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2},
  {Special1, Special3, Special4, Fireball, Fireball2, Super1}
};*/
  
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
  
 F_FHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
 
 F_FHaus(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
 }
  
public void specSetup(){
  name = "FamilyHouse";
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
      Standing.resetAnim = false;
      //Standing.ActEffs = new ActEffect[]{new ChangeAnimTo(jFallAnim, 1, new InAir(), new FalseCond(new AnimCheck(jFallAnim)) ), new ChangeAnimTo(Anim0, 1, new Grounded())};
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
  
  Falling.AttAnim = new Animation(new int[]{4, 4, 4}, 0, 3, "FHouse/FH-jumpfall/FH-jfall"); Falling.resetAnim = false; Falling.AttAnim.loop = false;
        
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, 0, 7, "FHouse/FH-fWalk/FH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 4, 0);//3, 0);
  
  bWalk = new Action(new Animation(new int[]{10,8,8,10,10,10}, 0, 6, "FHouse/FH-bWalk/FH-bwalk"), false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2.5f, 0);
  
  int[] times3 = {1, 2, 1, 4};
  Animation Anim3 = new Animation(times3, 0, 4, "FHouse/FH-n.jump/FH-n.jump");
  Anim3.loop = false;
  Jumping = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  Jumping.resetAnim = true;

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  Jumping.addAllLists(1, 2, 0, -20);
  
  fDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  fDiaJump.resetAnim = true;

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
      fDiaJump.addAllLists(1, 2, 11, -20);
  
  bDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  bDiaJump.resetAnim = true;

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bDiaJump.addAllLists(1, 2, -7.5f, -20);
  
  Jumping2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  
  //FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  FDash = new Action("FH-st-fdash", new Animation(new int[]{6, 2, 8, 4, 8}, 0, 5, "FHouse/FH-st-fdash/FH-st-fdash"),
  0, 0, 0, 0, false, false, false, false);
  FDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
   /* FDash.updFrameDataArr(0, 6);
    FDash.updFrameDataArr_float(0, 0, 0);
    FDash.addAllLists(1, 12, 20, 0);
    FDash.addAllLists(2, 2, 18, 0); *///function adds zero length collumn at end
    
  //BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  BDash = new Action("FH-st-bdash" ,new Animation(new int[]{4, 8, 6, 4}, 30, 4, "FHouse/FH-st-bdash/FH-st-bdash"),
  0, 0, 0, 0, false, false, false, false);
  BDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
  BDash.updFrameDataArr(0, 4);
  BDash.updFrameDataArr_float(0, 0, 0);
  BDash.addAllLists(1, 8, -10, 0);
  BDash.addAllLists(2, 10, -2.2f, 0);
  
  //FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  airFDash = new Action("FH-st-aifdash", new Animation(new int[]{6, 2, 8, 4, 8}, 0, 5, "FHouse/FH-st-fdash/FH-st-fdash"),
  0, 0, 0, 0, false, false, false, false);
  //airFDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
   /* airFDash.updFrameDataArr(0, 6);
    airFDash.updFrameDataArr_float(0, 0, 0);
    airFDash.addAllLists(1, 10, 23, 0);
    airFDash.addAllLists(2, 1, 23, 0);*/
    airFDash.allButSelfCancel = true;
    

  //BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  airBDash = new Action("FH-st-bdash", new Animation(new int[]{4, 8, 6, 4}, 30, 4, "FHouse/FH-st-bdash/FH-st-bdash"),
  0, 0, 0, 0, false, false, false, false);
  //airBDash.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, false), new ForceAddEff(2, 0, true)};
  airBDash.updFrameDataArr(0, 4);
  airBDash.updFrameDataArr_float(0, 0, 0);
  airBDash.addAllLists(1, 8, -14, 0);
  airBDash.addAllLists(2, 10, -4.2f, 0);
  
  int[][] FBvars = {{2, 3, 6}, {2, 3, 6, 9}};
  Condition[] CondFB0 = {new InAir(), new comfPButC(FBvars), new fPButCond(4)};
  int [] times7 = {4, 4, 4, 4, 3, 8, 11, 11};
  Animation Anim = new Animation(times7, 0, 8, "FHouse/FH-sp2-gr/FH-2sp-gr");
  Fireball = new FHaus_ProjAction(Anim, 20, 15, 12, 0, false, false, false, false, 1.99f, 80, 5.4f, 0, true, true, false, FB1AnimUp, FB1AnimMid, FB1AnimDown);
  Fireball.Conds = CondFB0; Fireball.attWeight = 30; Fireball.gravMult = 1.05f;
  Fireball.ActEffs = new ActEffect[]{new GravEff(0, 0, false), new GravEff(1, 2, true)};
  Fireball.updFrameDataArr(0, 19);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball.addAllLists(1, 25, 0, 0);
  Fireball.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball.ProjAnim = FB1AnimMid;
  Fireball.destrEffAnim = BettDestrEff;
  
  Condition[] CondFB1 = {new Grounded(), new comfPButC(FBvars), new fPButCond(4)};
  Fireball2 = new FHaus_ProjAction(Anim, 20, 15, 12, 0, false, false, false, false, 1.8f, 80, 5.4f, -5, true, true, false, FB1AnimUp, FB1AnimMid, FB1AnimDown);
  Fireball2.Conds = CondFB1; Fireball2.attWeight = 30;
  Fireball2.updFrameDataArr(0, 19);
  Fireball2.updFrameDataArr_float(0, 0, 0);
  Fireball2.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball2.addAllLists(1, 35, 0, 0);
  Fireball2.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball2.ProjAnim = FB1AnimMid;
  Fireball2.destrEffAnim = BettDestrEff;
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action("fh-cr", new Animation(times2, 0, 4, "FHouse/FH-cr/HF-cr"), false);
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

  
  /*int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD");
  //softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-hKD/FH-hKD");*/

  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  
    int[] times14 = {3, 2, 5, 2, 3};
  Condition[] Cond1 = {new fPButCond(4)};    
  Animation Anim14 = new Animation(times14, 0, 5, "FHouse/FH-st.Light/FH-st.Light");
  LightNormal = new Action("FH-st-light", Anim14, Cond1, 10, 6, 4, Action.MID, 1, true, false, false, false);
      LightNormal.ActEffs = new ActEffect[]{new SoundEff( "fh-st-l" , 0, 0), //new SelfCancEff(LightNormal), 
      }; 
      
  Condition[] Cond2 = {new fPButCond(6)};
    int[] times16 = {2, 2, 3, 2, 2, 5, 2, 1, 1, 2};
    Animation Anim16 = new Animation(times16, 50, 10,"FHouse/FH-st.Mid/FH-st.Mid");
  MidNormal2 = new Action("FH-st-med", Anim16, Cond2, 20, 12, 12, 3, 1, true, false, false, false);
  MidNormal2.ActEffs = new ActEffect[]{new SoundEff("swoosh_5", 0, 1)};
  
  Condition[] Cond3 = {new fPButCond(5)};
    int[] times17 = {2, 2, 3, 2, 4, 6, 2, 1, 1, 2, 2, 2};
    Animation Anim17 = new Animation(times17, 50, 12,"FHouse/FH-st.Heavy/FH-st.Heavy");
  HeavyNormal = new Action("FH-st-heay", Anim17, Cond3, 16, 12, 18, Action.MID, 1, true, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_0", 0, 1), new SoundEff("honk_0", 1, 1), new CounterEff(0, new CounterHit() )};
  
  int[] times18 = {2, 3, 5, 3, 2};
  Animation Anim18 = new Animation(times18, 25, 5, "FHouse/FH-cr.Light/FH-cr.Light");
  cr_LightNormal = new Action("FH-cr-light", Anim18, Cond1, 15, 6, 3, 3, 1, true, false, false, false);
  cr_LightNormal.ActEffs = new ActEffect[]{ new SoundEff("fh-cr-l", 0, 0)};
  
  int[] times19 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  Animation Anim19 = new Animation(times19, 50, 15, "FHouse/FH-cr.Mid/FH-cr.Mid");
  cr_MidNormal = new Action("FH-cr-med", Anim19, new Condition[]{new fPButCond(8)}, 26, 10, 10, Action.LOW, 1, true, true, true, false);
  cr_MidNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_2", 2, 1)};
  

  cr_MidNormal2 = new Action("FH-cr-m2", new Animation(new int[]{4, 3, 1, 2, 4, 1, 2, 2, 4, 2, 2}, 50, 11, "FHouse/FH-cr-m2/FH-cr-2m"), 
  Cond2, 18, 8, 8, Action.MID, 1, true, false, false, false); cr_MidNormal2.jumpCancel = true;
  cr_MidNormal2.ActEffs = new ActEffect[]{new SoundEff("swoosh_2", 2, 1), new OppEff(new SetForce(4, -5, 2), 2, new OppCheck(new InAir()), new ActTimeCond3(3, 8) ), new FirstHitEff(3, 2, true) };
  
    int[] times23 = {2, 4, 2, 2, 3, 3, 5, 2, 3, 2, 2, 2};
  cr_HeavyNormal = new Action( "FH-cr-heay", new Animation(times23, 30, 12, "FHouse/FH-cr.Heavy/HF-cr.Heavy"), Cond3,
                               16, 8, 16, Action.MID, 1, true, false, false, false);
       cr_HeavyNormal.ActEffs = new ActEffect[]{new SoundEff(7, 0, 0), new CounterEff(2, new CounterHit() )};
       cr_HeavyNormal.jumpCancel = true;
  
  int[] times20 = {2, 1, 4, 1, 2};
  Animation Anim20 = new Animation(times20, 25, 50, 5, "FHouse/FH-j.Light/FH-j.Light");
  j_LightNormal = new Action("FH-j-light", Anim20, Cond1, 12, 4, 3, Action.AIR, 1, true, true, false, false);
  //j_LightNormal.selfCancel = true;
  j_LightNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_2", 1, 1)};
    
  int[] times22 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  j_MidNormal = new extraForceOnHitAct( "FH-j-med", new Animation(times22, 0, 90, 10, "FHouse/FH-j.med/HF-j.med"),
                               16, 8, 10, Action.AIR, true, true, false, false, 3, -16);
            Condition[] Cond20 = {new ButCond(6), new ButCond(1)};
            j_MidNormal.Conds = Cond20; j_MidNormal.attWeight = 2;
            j_MidNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_4", 0, 0), new CounterEff(3)};
                               
  int[] times28 = {2, 3, 2, 2, 4, 2, 3};
  j_MidNormal2 = new Action( "Fh-j-2med", new Animation(times28, 50, 40, 7, "FHouse/FH-j-2med/FH-j-2med"), Cond2,
                               14, 8, 8, Action.AIR, 1, true, true, false, false);
                               j_MidNormal2.ActEffs = new ActEffect[]{new SoundEff("swoosh_2", 0, 0)};
                               j_MidNormal2.jumpCancel = true; j_MidNormal2.dashCancel = true;
                               
  
  int[] times21 = {3, 4, 3, 4, 3, 2, 2};
  Animation Anim21 = new Animation( times21, 50, 50, 7, "FHouse/FH-j.Heavy/FH-j.Heavy");
  j_HeavyNormal = new Action("FH-j-heay", Anim21, Cond3, 9, 5, 9, Action.AIR, 1, true, true, false, false);
  j_HeavyNormal.updFrameDataArr_float(1, -6, -10);
  j_HeavyNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_5", 0, 0), new CounterEff(3, new CounterHit())};
  
  j_DustNormal = new Action("FH-j-t", new Animation( new int[]{3, 3, 3, 2, 2, 5, 3, 3}, 40, 10, 8, "FHouse/FH-j-t/FH-j-t"), new Condition[]{new fPButCond(8)}, 13, 7, 9, Action.AIR, 1, true, true, false, false);
  //j_DustNormal.updFrameDataArr_float(1, -6, -10);
  j_DustNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_5", 0, 0), new OppEff( new SetForce(0, -8, 2), 2, new OppCheck(new InAir()) )};
  
  int[] times24 = {2, 2, 2, 2, 4, 2, 2, 2};
  Condition[] Cond4 = {new comfPButC(new int[][]{{6, 2, 3}, {6, 3, 2, 3, 6}, {6, 3, 2, 3}, {6, 2, 6}}), new fPButCond(5)};
  Special1 = new Action("FH-sp1", new Animation( times24, 50, 8, "FHouse/FH-sp1/FH-1sp"), Cond4, 18, 16, 6, Action.MID, 20, true, true, true, false);
  Special1.ActEffs = new ActEffect[]{new ForceAddEff(0, 0, true), new ForceAddEff(8, 0, false), new ForceAddEff(12, 0, false)};
   
  Condition[] Cond5 = {new facingCond(1), new fPButCond(5)};
   int[] times25 = {4, 3, 6, 2, 3, 6, 3, 3, 3};
   st_fHeavy = new Action("FH-st-forH",//"data/FH-st-fHeavy", 
   new Animation( times25, 75, 9, "FHouse/FH-st.fH/FH-st-fH"), Cond5, 20, 14, 6, Action.HIGH, 2, true, false, false, false);
   
    int[] times21p3 = {4, 4, 4, 4, 4};
    Action Special3p3 = new Action( //"HH-cr-Light",
    new Animation(times21p3, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-3p"), 20, 10, 4, 0, true, true, false, false);
    
    Action FillAct = Special3p3; Special3p3.attWeight = 20;
    FillAct.updFrameDataArr(0, 20);
  FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p2 = {2, 2, 2};
    Action Special3p2 = new Action( "FH-sp3p2",
    new Animation(times21p2, 25, 50, 3,"FHouse/FH-sp3/FH-3sp-2p"), 20, 10, 20, Action.HIGH, true, false, true, false);    
         FillAct = Special3p2; Special3p2.attWeight = 20;
    FillAct.updFrameDataArr(0, 60); FillAct.updFrameDataArr_float(0, 16, 18);
    Special3p2.ActEffs = new ActEffect[]{ new AddOwnForcToOpp(2,1, new Condition()), new ChangeActTo(Special3p3, 1, new Grounded()) };

    
    int[][] revFBvars = {{2, 1, 4}, {2, 1, 4, 7}};
    Condition[] Cond6 = {new InAir(), new comfPButC(revFBvars), new fPButCond(4)};
    int[] times21p1 = {2, 2, 2, 2, 2};
    Special3 = new Action( //"HH-cr-Light",
    new Animation(times21p1, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-1p"), 6, 5, 4, Action.AIR, false, false, false, false); 
    Special3.Conds = Cond6; Special3.attWeight = 20;
    Special3.ActEffs = new ActEffect[]{new ChangeActTo(Special3p2, 1, new ActTimeCond2(0, 9) )};
          FillAct = Special3;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
    Condition[] Cond7 = {new comfPButC(revFBvars), new fPButCond(5)};
     int[] times26 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  Special4 = new Action("FH-sp4", new Animation( times26, 0, 14, "FHouse/FH-sp4/FH-4sp"), Cond7, 32, 10, 10, Action.HIGH, 20, true, true, true, true);
            FillAct = Special4;
    //FillAct.updFrameDataArr(0, 28);
    FillAct.updFrameDataArr_float(1, 16, -22);
    FillAct.updFrameDataArr_float(2, 4, 0);
    FillAct.updFrameDataArr_float(4, 4, 0);
   //FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
   int[][] superm = {{2, 3, 6, 2, 3, 6}, {2, 3, 6, 2, 3}, {2, 6, 2, 6}, {2, 3, 6, 2, 3, 6, 9}};
       Condition[] Cond8 = {new SuperCond(500), new comfPButC(superm), new fPButCond(6)};
     int[] times27= {3, 3, 3, 3, 3,
   3, 3, 3, 3, 3,
 3, 3, 3, 3, 3, 3};
  Super1 = new Action("FH-1super", new Animation( times27, 95, 16, "FHouse/FH-1super/FH-1super"), Cond8, 20, 18, 10, Action.MID, 50, true, false, false, true);
   Super1.superNeed = 500;
   
  LightNormal.addEffs(new GatlingEff(MidNormal2), new GatlingEff(new Condition[]{new ButCond(1)}, cr_LightNormal));
  MidNormal2.addEffs(new GatlingEff(new Condition[]{new ButCond(1)}, cr_HeavyNormal, cr_MidNormal2), new GatlingEff(st_fHeavy, HeavyNormal));
  HeavyNormal.addEffs(new GatlingEff(st_fHeavy));
  cr_LightNormal.addEffs(new GatlingEff(LightNormal, MidNormal2), new GatlingEff(new Condition[]{new ButCond(1)}, cr_MidNormal));
  cr_MidNormal2.addEffs(new GatlingEff(HeavyNormal), new GatlingEff(new Condition[]{new ButCond(1)}, cr_MidNormal, cr_HeavyNormal));
  cr_HeavyNormal.addEffs(new GatlingEff(MidNormal2), new GatlingEff(new Condition[]{new ButCond(1)}, cr_MidNormal2));
  j_LightNormal.addEffs(new GatlingEff(j_MidNormal2));
  j_MidNormal2.addEffs(new GatlingEff(j_LightNormal, j_HeavyNormal, j_DustNormal));
  j_DustNormal.addEffs(new GatlingEff(j_MidNormal2, j_HeavyNormal));
   
    Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, st_fHeavy, FDash, BDash, Jumping, fDiaJump, bDiaJump, Special4, Fireball2, Special1, Super1, NormalThrow},
  {cr_LightNormal, cr_MidNormal2, cr_HeavyNormal, cr_MidNormal, Special1, Super1},
  {j_LightNormal,j_MidNormal2, j_MidNormal, j_HeavyNormal, j_DustNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2, Fireball, Special1, Special3, Special4, AirThrow},
  { Fireball, Fireball2, Special1, Special3, Special4, Super1}
};

  fillActionsList(ActTab);
   
}

public void airDash(){
  if(cancelWindow <= 0 || CurAction.dashCancel) ; else return;
  if(curAirActions > 0){
          if(compareBufferWithCombAtt(fDashmotion)){
            CurAction.reset();
          CurAction = airFDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
    if(compareBufferWithCombAtt(bDashmotion)){
            CurAction.reset();
          CurAction = airBDash;
          //inputbufferDir.set(inputbufferDir.size()-1, 5);
          curAirActions--;
    }
  }
}

  public void st_Normals(){
        if(CurAction == fWalk){
     // Force.x =  dirMult * sqrt(sq(sin(fWalk.AttAnim.timer* 0.1))) * 6;
    }
  }
  
  public void cr_Normals(){
  }
  
  public void j_Normals(){
    }   
         
}

//HOCHHAUS###############################################################################################################################################################################################

class F_HHaus extends Fighter{
  Action RoofNormal, BaseNormal, j_MidNormal2;
  Action Special1, Special2, Special4CG, Special5detPlane;
  Animation FB1Anim, FB1DestrEff, FirewallAnim,  FirewallAnimp2;
  ProjAction Fireball, Fireball2, Fireball3;
  Projectile Firewall;
  
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
 
  F_HHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
  }
  
  F_HHaus(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
  }
 
   public void copyStdActions(Fighter F){
        Standing = new Action(F.Standing); Crouching = new Action(F.Crouching);
    Knockdown = new Action(F.Knockdown); softKD = new Action(F.softKD); WallStick = new Action(F.WallStick); WallBounce = new Action(F.WallBounce);
    Juggle = new Action(F.Juggle); Stagger = new Action(F.Stagger); BeingGrapped = new Action(F.BeingGrapped);
    InHitStun = new Action(F.InHitStun); HHit = new Animation(F.HHit); LHit = new Animation(F.LHit);
    Blocking = new Action(F.Blocking); HBlock = new Animation(F.HBlock); LBlock = new Animation(F.LBlock);
    Jumping = new Action(F.Jumping); fDiaJump = new Action(F.fDiaJump); bDiaJump = new Action(F.bDiaJump);
    //Jumping2 = new Action(F.Jumping2); fDiaJump2 = new Action(F.fDiaJump2); bDiaJump2 = new Action(F.bDiaJump2); // HHaus has no double jump -> reason for nullptrexcept
    fWalk = new Action(F.fWalk); bWalk = new Action(F.bWalk); //FDash = new Action(F.FDash); 
    BDash = new Action(F.BDash); // maybe adding costume constructor
  }

public void specSetup(){
  name = "HochHouse";
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
      
  Falling.AttAnim = new Animation(new int[]{4}, 0, 30, 1, "HHouse/HH-jumpfall/HH-jfall");
      
  int[] times2 = {5, 5, 5, 5, 5, 5, 5};
  Animation Anim1 = new Animation(times2, 30, 7,"HHouse/HH-fWalk/HF-fWalk");
      
  fWalk = new Action(//"HH-HeavyNormal",
  Anim1, 0, 0, 0, 0, true, false, false, false);
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 2.8f, 0);
  fWalk.resetAnim = false;
  
  bWalk = new Action(//"HH-HeavyNormal",
  Anim1, 0, 0, 0, 0, true, false, false, false);
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -1.8f, 0);
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
  
  /*int[] ani4 = {1, 12, 1, 1};
  FDash = new Action(ani4, 0, 0, 0, 0, false, false, false, false);
  
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 10, 0);
    FDash.addAllLists(1, 5, 7, 0);*/
    
  int[] ani5 = {1, 11, 1, 1};
  BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, -5, 0);
  BDash.addAllLists(1, 5, -4, 0);
  
  int[] aniFB2 = {11, 7, 7};
  Condition[] CondFB1 = {new Grounded(), new fPButCond(6), new dirCombCond(new int[]{2,3,6})};
    Fireball2 = new HHaus_ProjAction2(aniFB2, 20, 15, 2, 0, false, false, true, false, 1, -1, 4.0f, 0, false, false, false);
    Fireball2.Conds = CondFB1; Fireball2.attWeight = 18;
  Fireball2.updFrameDataArr(0, 25);
  Fireball2.updFrameDataArr_float(0, 0, 0);
  Fireball2.addAllLists(1, 40, 0, 0);
  
  int[] aniFB3 = {11, 3, 3};
    Condition[] CondFB2 = {new Grounded(), new fPButCond(5), new dirCombCond(new int[]{2,1,4})};
    Fireball3 = new HHaus_ProjAction3(aniFB3, 0, 0, 0, 0, false, false, false, false, 1.2f, 200, 2.0f, 0, true, true, false);
    Fireball3.Conds = CondFB2; Fireball3.attWeight = 18;
  Fireball3.updFrameDataArr(0, 25);
  Fireball3.updFrameDataArr_float(0, 0, 0);
  Fireball3.addAllLists(1, 40, 0, 0);
  
    int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Crouching = new Action(//"bn-cr", 
  new Animation(times1, 0, 7, "HHouse/HH-cr.idle/HH-cr.idl"), false);
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
  
   /* int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"HHouse/HH-sKD/HH-air-sKD");
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"HHouse/HH-hKD/HH-hKD");*/
  
    int[] ani13 = {11};
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 160, 300, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
  int[] ani12 = {0, 9, 9};
  RoofNormal = new Action(ani12,  5, 2, 30, Action.HIGH, true, true, false, false);
  RoofNormal.updFrameDataArr(0, 15); 
  RoofNormal.updFrameDataArr_float(0, 0, 0);
  RoofNormal.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  RoofNormal.addAllLists(1, 4, 3, 0);
  RoofNormal.HitBoxCollect.get(1).add(new ColCircle( 150, -125, 160, 300, 0, 0, -1));
  RoofNormal.HurtBoxCollect.get(1).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  RoofNormal.addAllLists(2, 20, 0, 0);
  RoofNormal.HurtBoxCollect.get(2).add(new ColCircle( 0, -125, 160, 300, 0, 0, -1));
  
     int[] times15 = {2, 3, 1, 1, 2, 4};
     Condition[] Cond0 = {new fPButCond(4)};
  LightNormal = new Action("HH-st-light", new Animation(times15, 50, 6,"HHouse/HH-st.Light/HH-st.Light"), Cond0, 10, 5, 2, 0, 1, false, false, false, false);
  
   int[] times16 = {1, 1, 1, 1, 2, 2, 2, 3, 1, 1, 1, 1, 1, 1};
   Condition[] Cond1 = {new fPButCond(6)};
  MidNormal2 = new Action("HH-st-mid", new Animation(times16, 100, 14,"HHouse/HH-st.Mid/HH-st.Mid"), Cond1, 18, 16, 8, 0, 6, false, false, false, false);

  int[] times17 = {6, 6, 2, 2, 3, 2, 4, 7, 3, 3}; //35
  Condition[] Cond2 = {new fPButCond(5)};
  HeavyNormal = new Action("HH-HeavyNormal",
  new Animation(times17, 150, 10,"HHouse/HH-st.Heavy/HH-st.Heavy"), Cond2, 16, 10, 16, 0, 7, false, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.STAGGER, new CounterHit())};
  
    int[] times18 = {4, 5, 3, 2};
    cr_LightNormal = new Action( "HH-cr-Light",
    new Animation(times18, 25, 4,"HHouse/HH-cr.Light/HH-cr.Light"), Cond0, 12, 6, 4, 0, 2, false, false, false, false);
    
        int[] times19 = {3, 4, 4, 2, 4, 3, 2};
    cr_MidNormal = new Action( "HH-cr-med",
    new Animation(times19, 75, 7,"HHouse/HH-cr.med/HH-cr.med"), new Condition[]{new ButCond(1), new fPButCond(8)}, 
    20, 8, 12, Action.LOW, 7, false, false, true, false);
    
    cr_MidNormal2 = new Action( "HH-cr-m2", new Animation(new int[]{4, 4, 2, 4, 4}, 25, 5,"HHouse/HH-cr-m2/HH-cr-2m"),
    Cond1, 18, 8, 8, Action.MID, 6, true, false, false, false);
    
        int[] times20 = {5, 5, 2, 2, 5, 3, 3};
    cr_HeavyNormal = new Action( "HH-cr-Heay",
    new Animation(times20, 25, 7,"HHouse/HH-cr.Heavy/HH-cr.Heavy"), Cond2, 20, 15, 30, 0, 7, false, false, false, false);
    cr_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.JUGGLE, new CounterHit()) };
    cr_HeavyNormal.jumpCancel = true;
    
    
    int[] times21p3 = {2, 2, 2, 2};
    Action j_MidNormalp3 = new Action( //"HH-cr-Light",
    new Animation(times21p3, 30, 4,"HHouse/HH-j.med/p3/HF-j.med"), 6, 5, 4, 0, true, true, false, false);
    
    Action FillAct = j_MidNormalp3;
    FillAct.updFrameDataArr(0, 10);
  FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p2 = {2, 2};
    Action j_MidNormalp2 = new Action( "HH-j-medp2",
    new Animation(times21p2, 30, 2,"HHouse/HH-j.med/p2/HF-j.med"), 6, 5, 4, 0, false, false, false, true);
      j_MidNormalp2.ActEffs = new ActEffect[]{ 
      new SetForce(10, 0, 1, new facingCond(1)), new SetForce(-10, 0, 1, new facingCond(-1)), 
      new ChangeActTo(j_MidNormalp3, 1, new FalseCond(new ButCond(6)) ),
      new ChangeActTo(j_MidNormalp3, 1, new ActTimeCond2(0, 20))};
    
    int[] times21p1 = {2, 2, 2, 2, 2};
    j_MidNormal = new Action( //"HH-cr-Light",
    new Animation(times21p1, 30, 5,"HHouse/HH-j.med/p1/HF-j.med"), 6, 5, 4, 0, false, false, false, false); 
    j_MidNormal.Conds = Cond1; j_MidNormal.attWeight = 3;
          FillAct = j_MidNormal;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   j_MidNormal.ActEffs = new ActEffect[]{ new ChangeActTo(j_MidNormalp2, 1, new ActTimeCond2(0, 10) ) };
   
   
   Condition[] Cond3 = {new ButCond(1), new fPButCond(6)};
   int[] times29 = {4, 3, 6, 3, 3};
   j_MidNormal2 = new Action( "HH-j-2med",
    new Animation(times29, 0, 5,"HHouse/HH-j-2med/HH-j-2med"),Cond3 , 12, 8, 10, Action.AIR, 5, false, false, false, false); 
    j_MidNormal2.updFrameDataArr_float(1, 0, 16);
   
      int[] times22 = {3, 2, 4, 2};
    j_LightNormal = new Action( "HH-j-light",
    new Animation(times22, 30, 4,"HHouse/HH-j.light/FH-j.light"), Cond0, 8, 4, 4, Action.AIR, 1, true, true, false, false); 
    j_LightNormal.jumpCancel = true;
    
          int[] times23 = {2, 2, 2, 2, 4, 2, 4, 2, 2, 2, 2};
    j_HeavyNormal = new Action( "HH-j-heay",
    new Animation(times23, 50, 30, 11,"HHouse/HH-j.Heavy/FH-j.Heavy"), Cond2, 10, 7, 20, Action.HIGH, 6, true, true, true, false); 
    j_HeavyNormal.gravMult = 0.93f; j_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.WALLSTICK, new Condition[]{new CounterHit()}), new SoundEff("hf-j-h-pumpit", 0, 0)}; //GEFAHR
   
   int[] backCharge = {6}; 
   Condition[] Cond4 = {new ChargeDirCheck(20, 1), new fPButCond(5), new dirCombCond(backCharge),
  };      
  
  int[] revFB = {2, 1, 4}, FB = {2, 3, 6};
  Condition[] CondFB0 = {new Grounded(), new fPButCond(4), new dirCombCond(FB)};
  int[] times7 = {3, 3, 3, 3, 4, 4, 4, 3, 2, 3, 3};
  Fireball = new HHaus_ProjAction1( new Animation(times7, 0, 11, "HHouse/HH-sp2/HH-2sp"),
  18, 30, 10, 0, false, false, false, false, 1, 300, -6.0f, 0, false, false, true);
  Fireball.Conds = CondFB0; Fireball.attWeight = 18;
  Fireball.ProjAnim = FB1Anim;
  Fireball.destrEffAnim = FB1DestrEff;
  Fireball.updFrameDataArr(0, 15);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.HurtBoxCollect.get(0).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  Fireball.addAllLists(1, 20, 0, 0);
  Fireball.HurtBoxCollect.get(1).add(new ColCircle( 0, -150, 170, 300, 0, 0, -1));
  
            int[] times24 = {3, 3, 2, 6, 2, 2, 4, 4, 2, 2};
    Special1 = new Action( "HH-sp1",
    new Animation(times24, 30, 10,"HHouse/HH-sp1/HH-1sp"),Cond4, 15, 7, 12, Action.MID, 20, true, true, false, false); 
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
  
     Condition[] Cond5 = {new Grounded(), new fPButCond(4), new dirCombCond(revFB)};
      int[] times26 = {2, 2, 2, 3, 6, 6, 6, 3, 3};
  Special2 = new ChangeAction("HH-sp3-p1", new Animation(times26, 0, 9,"HHouse/HH-sp3/HH-3sp-1p"),
  5, 2, 5, -1, true, true, false, false, NormalGrab);
  Special2.Conds = Cond5; Special2.attWeight = 20;
 /* Special2 .updFrameDataArr(0, 6); 
  
    Special2 .HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  Special2 .addAllLists(1, 12, 3, 0);
  
  Special2 .HurtBoxCollect.get(1).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  Special2 .addAllLists(2, 10, 0, 0);
  Special2 .HurtBoxCollect.get(2).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));*/
    Special2 .HitBoxCollect.get(1).add(new ColCircle( 10, -270, 280, 200, 0, 0, -1));
    Special2 .updFrameDataArr_float(0, 10, -20);
    
      float[][] xyGrab1 = {{200, -80},
                      {200, -80},
                      {210, -84}, 
                    {230, -88}};
  Action Special4CGp2 = new Action("HH-sp4cgp2", 
  new Animation( new int[]{6, 4, 2, 4, 4,    4, 4, 4, 4, 6,   4, 4, 4, 4, 4} , 150, 14, "HHouse/HH-sp4CG/p2/HH-4sp"), 
  new Condition[]{new Condition()}, 
  2, 0, 0, Action.GRAB, 1, true, false, false, false);
 Special4CGp2.ActEffs = new ActEffect[]{new GrabEff(xyGrab1 , new int[]{16,6,6,6}), new PutInHitSt(30, 34, new ActTimeCond2(0,36) ), new OppEff(new SetForce(-26, -8, 1), 1, new ActTimeCond2(0,36) )};
 Special4CGp2.hitCancel = false;

  Special4CG = new Action("HH-sp4cg", 
  new Animation( new int[]{2, 4, 2, 4, 2,   4, 2, 4, 2} , 80, 8, "HHouse/HH-sp4CG/HH-4sp"), 
 new Condition[]{new fPButCond(5), new comfPButC(new int[][]{ {2,3,6} } )}, 
  10, 0, 0, Action.GRAB, 30, true, false, false, false);
 Special4CG.ActEffs = new ActEffect[]{new ChangeActTo(Special4CGp2, 0, new Condition())};
 Special4CG.hitCancel = false;
 
  /*  OilProj = new WH_Oilpuddle(0.0, 0.0, 200, 0, 200, 100, 0.0, 0.0, 1, 600, 0, 0, 0, false, false, false, false, false) ;
   OilProj.setAnims(new Animation(new int[]{10}, 0, -25, 1, "Projectiles/Oilpuddle/Oilpuddle"), new Animation(new int[]{10}, 0, -25, 1, "Projectiles/Oilpuddle/Oilpuddle"));
                  int[] times32 = {2, 6, 2, 6, 2,
                2, 2, 6, 2, 2,
              2, 3, 2};
         Condition[] Cond8 = {new fPButCond(5), new dirCombCond(revFB)};
   Special5 = new Action("Wh-sp2",
   new Animation( times32, 100, 10, "WHouse/WH-sp5-oil/WH-5sp-oil"), Cond8, 32, 10, 4, Action.MID, 18, true, false, false, false);
   Special5.ActEffs = new ActEffect[]{ new ProjAddEff( OilProj, 1, new ActTimeCond(0, 1) )};*/
 
 FirewallAnim = new Animation(new int[]{4, 4, 4, 4,   4, 4, 4, 4}, 0, 0, 8, "Projectiles/PlaneFirewall/PlaneFW"); 
 FirewallAnimp2 = new Animation(new int[]{4, 4, 4, 4}, 0, -420, 4, "Projectiles/PlaneFirewall/p2/PlaneFW");
 Firewall = new Projectile(0.0f, 0.0f, 0, 0, 70, 860, -3.0f, 0.0f, 1, 32, 50, 20, 8, false, false, false, true, true);
 Firewall.setAnims(FirewallAnim, FirewallAnimp2);
 
  Special5detPlane = new Action("HH-sp5", 
  new Animation(new int[]{2, 4, 8, 2, 8, 2, 2, 8, 2, 2, 2, 2 }, 50, 12,"HHouse/HH-sp5/HH-5sp"), 
  new Condition[]{new fPButCond(6), new comfPButC(new int[][]{ {2,1,4} } )}, 
  18, 16, 8, 0, 32, true, false, false, false);
  //Special5detPlane.ActEffs = new ActEffect[]{ new ProjAddEff( Firewall, 1, new ActTimeCond(0, 1) )};
  
  Action Special6trip = new Action("HH-sp6", 
  new Animation(new int[]{4, 4, 2, 4, 2, 4, 8, 4, 3, 3 }, 140, 10,"HHouse/HH-sp6/HH-6sp"), 
  new Condition[]{new Grounded(), new fPButCond(6), new comfPButC(new int[][]{ {2,3,6} })},//{4,1,2,3,6}, {4,2,6}, {4,1,2,3,6} } )}, 
  36, 20, 14, Action.HIGH, 32, true, false, false, false);
  Special6trip.ActEffs = new ActEffect[]{new CounterEff(Action.GROUNDBOUNCE, new CounterHit())};
    
        Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, Special2, Fireball,//Fireball2, Fireball3, 
Special1, Special5detPlane, Special4CG, Special5detPlane, Special6trip, NormalThrow},
  {cr_LightNormal, cr_MidNormal2, cr_MidNormal, cr_HeavyNormal},
  {j_LightNormal, j_MidNormal, j_MidNormal2, j_HeavyNormal, AirThrow},
  {Special1, Fireball, Special6trip}
};
  fillActionsList(ActTab);
}

public void dash(){
      if(compareBufferWithCombAtt(bDashmotion) && (y >= GROUNDHEIGHT || CollisionBox.bottom)
      ){
          CurAction.reset();
          CurAction = BDash;
          inputbufferDir.set(inputbufferDir.size()-1, 5);
    }
}

public void airDash(){}

public void st_Normals(){  
}
public void cr_Normals(){
}
public void j_Normals(){
}

public void extraStuff(){
  if(CurAction == Special5detPlane && CurAction.curCollumn == 0 && CurAction.curMoveDur == 1){
    for(int i = Projectiles.size()-1; i >= 0; i--){
      Projectile p = Projectiles.get(i), 
      p2 = Firewall.copy(); p2.setXY(dirMult, p.x, p.y);
      p2.setAnims(FirewallAnim, FirewallAnimp2);
      p.exTimer = 0;
      Projectiles.set(i, p2);
      
    }
  }
}



}


//Wohnwagen###############################################################################################################################################################################################
class F_WHaus extends Fighter{
  
  boolean stance = false;
  int gearLevel = 0, gearTime = 0;
  
  WH_Oilpuddle OilProj; Projectile SpeedProj;
  Action fDash_gl0, fDash_gl3, bDash_gl0, bDash_gl3, Stance, stanceFWalk, stanceBWalk, StanceFDash;
  Action StanceLight, StanceMed, StanceHeavy;

  Action Special1, Special2, Special3rek1, Special3rek2, Special4, Special5, 
  Special6drill, Sp7speedProj, Sp8speedDP, Super1RTL;
  
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
  
  F_WHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
  }
  
   F_WHaus(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
  }
  
public void specSetup(){
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
      
  Falling.AttAnim = new Animation(new int[]{4}, 0, 1, "WHouse/WH-jumpfall/WH-jfall");
  Landing.AttAnim = new Animation(new int[]{3}, 0, 1, "WHouse/WH-landing/WH-landing");
  Turning.AttAnim = new Animation(new int[]{3,3,3},  0, 3, "WHouse/WH-turning/WH-turning");
  
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times0, 0, 1, "WHouse/WH-fWalk/WH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 4.2f, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -3, 0);
  
  Animation Anim1_1 = new Animation(times0, 0, 1, "WHouse/WH-stance/WH-stance");
      Stance = new Action( Anim1_1, false);
      Stance.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      Stance.updFrameDataArr(0, 1);
     Stance.updFrameDataArr_float(0, 0, 0);

  Animation Anim2_1 = new Animation(times0, 0, 1, "WHouse/WH-stance-fWalk/WH-stance-fW");
  stanceFWalk = new Action(Anim2_1, false);//0, 0, 0, 0, true, false, false, false);
  stanceFWalk.addingForce = true;
  stanceFWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  stanceFWalk.updFrameDataArr(0, 1);
  stanceFWalk.updFrameDataArr_float(0, 1.6f, 0);//3, 0);
  
  Animation Anim2_2 = new Animation(times0, 0, 1, "WHouse/WH-stance-bWalk/WH-stance-bW");
  stanceBWalk = new Action(Anim2_2, false);
  stanceBWalk.addingForce = true;
  stanceBWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -125, 200, 160, 0, 0, -1));
  stanceBWalk.updFrameDataArr(0, 1);
  stanceBWalk.updFrameDataArr_float(0, -1.2f, 0);
  
  int[] times3 = {2, 3};
  Animation Anim3 = new Animation(times3, 0, 2, "WHouse/WH-nJump/WH-nJump");
  Anim3.loop = false;
  Jumping = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.addAllLists(1, 2, 0, -18);
  
  fDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.addAllLists(1, 2, 10, -15);
  
  bDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.addAllLists(1, 2, -7.5f, -15);
  
    Jumping2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
 int[] ani4 = {8, 2, 4, 4, 2};
  fDash_gl0 = new Action("WH-fdash", new Animation(ani4, 0, 5, "WHouse/WH-dash/WH-fdash"),
  0, 0, 0, 0, false, false, false, false);
  
  airFDash = new Action("WH-fdash", new Animation(ani4, 0, 5, "WHouse/WH-dash/WH-fdash"),
  0, 0, 0, 0, false, false, false, false);
  airFDash.allButSelfCancel = true;
    
  fDash_gl3 = new Action( "WH-tpfdash", new Animation(new int[]{4, 2, 2, 2, 2, 4, 2, 2, 4}, 70, 9, "WHouse/WH-tpdash/WH-tpdash"),
  5, 0, 0, 0, false, false, false, false);
  fDash_gl3.collision = false; fDash_gl3.pushBackMult = 0;
    
  FDash = fDash_gl0;
    
  int[] ani5 = {8, 2, 12, 2};
  bDash_gl0 = new Action("WH-bdash", new Animation(ani5, 0, 4, "WHouse/WH-dash/WH-bdash"), 0, 0, 0, 0, false, false, false, false);
  
  airBDash = new Action("WH-bdash", new Animation(ani5, 0, 4, "WHouse/WH-dash/WH-bdash"), 0, 0, 0, 0, false, false, false, false);
  
    bDash_gl3 = new Action( "WH-tpbdash", new Animation(new int[]{4, 2, 2, 2, 2, 4, 2, 2, 4}, 70, 9, "WHouse/WH-tpdash/WH-tpdash"),
  5, 0, 0, 0, false, false, false, false);
  bDash_gl3.collision = false; bDash_gl3.pushBackMult = 0;
  
  BDash = bDash_gl0;
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action("wh-cr", new Animation(times2, 0, 1, "WHouse/WH-cr/WH-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "WHouse/WH-Stun/WH-HHitSt"); LHit = new Animation(times1, 0, 1, "WHouse/WH-Stun/WH-LHitSt");
  HBlock = new Animation(times1, 0, 1, "WHouse/WH-Stun/WH-HBlockSt"); LBlock = new Animation(times1, 0, 1, "WHouse/WH-Stun/WH-LBlockSt");
  
  int[] ani9 = {6};
  Blocking = new Action(ani9);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 200, 160));
  
  int[] ani10 = {2};
  InHitStun = new Action(ani10, 0, 0, 0, 0, true, true, false, false);
  InHitStun.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160));
  
  /*softKD.AttAnim = new Animation(times0_0, 0, 1,"WHouse/WH-sKD/WH-air-sKD");  
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"WHouse/WH-hKD/WH-hKD");*/
  
  int[] ani13 = {11};
  BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  BeingGrapped.HurtBoxCollect.get(0).add(new ColCircle( 0, 0, 200, 160, 0, 0, -1));
  BeingGrapped.updFrameDataArr(0, 10); 
  BeingGrapped.updFrameDataArr_float(0, 0, 0);
  
    int[] times14 = {4, 2, 4, 2, 2};
    Condition[] Cond0 = {new fPButCond(4)};
  Animation Anim14 = new Animation(times14, 0, 5, "WHouse/WH-st-light/WH-st-light");
  LightNormal = new Action("WH-st-light", Anim14, Cond0, 15, 7, 5, 3, 1, true, true, false, false);
  
    int[] times16 = {2, 4, 4, 3, 2, 3, 5, 2, 2, 1, 1};
    Condition[] Cond1 = {new fPButCond(6)};
    Animation Anim16 = new Animation(times16, 70, 11,"WHouse/WH-st-med/WH-st-med");
  MidNormal2 = new Action("WH-st-med", Anim16, Cond1, 20, 12, 15, 3, 4, true, true, false, false);
  MidNormal2.ActEffs = new ActEffect[]{new SoundEff("swoosh_2", 0, 1), new SoundEff("metalclank_0", 1, 2), new CounterEff(Action.STAGGER, new CounterHit())};
  
    int[] times17 = {4, 4, 4, 2, 6, 6, 2, 2, 2};
    Condition[] Cond2 = {new fPButCond(5)};
    Animation Anim17 = new Animation(times17, 70, 9,"WHouse/WH-st-heay/WH-st-heay");
  HeavyNormal = new Action("WH-st-heay", Anim17, Cond2, 22, 16, 20, Action.MID, 7, true, true, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{new SoundEff("swoosh_0", 0, 1), new SoundEff("bite_0", 2, 1)};
  
  int[] times18 = {5, 8, 2};
  Animation Anim18 = new Animation(times18, 0, 3, "WHouse/WH-cr-light/WH-cr-light");
  cr_LightNormal = new Action("WH-cr-light", Anim18, Cond0, 14, 7, 4, 3, 2, true, false, false, false);

  int[] times19 = {4, 4, 2, 2, 4, 4, 2, 2, 2, 4, 4};
  Animation Anim19 = new Animation(times19, 70, 11, "WHouse/WH-cr-med/WH-cr-med");
  cr_MidNormal = new Action("Wh-cr-med", Anim19, new Condition[]{new fPButCond(8)}, 40, 10, 4, Action.LOW, 8, true, true, true, false);
  
   cr_MidNormal2 = new Action( "Wh-cr-m2", new Animation(new int[]{3, 3, 3, 2, 6, 4, 4}, 0, 7,"WHouse/WH-cr-m2/WH-cr-2m"),
    Cond1, 18, 9, 8, Action.MID, 6, true, false, false, false);
  
    int[] times23 = {6, 3, 3, 6, 4, 4, 2, 6};
  cr_HeavyNormal = new Action( "Wh-cr-heay", new Animation(times23, 0, 8, "WHouse/WH-cr-heay/WH-cr-heay"), Cond2,
                               24, 14, 18, Action.MID, 8, true, false, true, false);
                               cr_HeavyNormal.jumpCancel = true;
  cr_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.JUGGLE, new CounterHit()), 
  new CounterEff(Action.JUGGLE, new WH_gearCheck(2, this))};
  
  int[] times20 = {3, 2, 4, 3};
  Animation Anim20 = new Animation(times20, 30, 0, 4, "WHouse/WH-j-light/WH-j-light");
  j_LightNormal = new Action("Wh-j-light", Anim20, Cond0, 8, 4, 3, Action.AIR, 1, true, true, false, false);
  j_LightNormal.jumpCancel = true; j_LightNormal.dashCancel = true;
  
  int[] times22 = {2, 2, 3, 2, 4, 2, 2, 3};
  j_MidNormal = new Action( "Wh-j-med", 
  new Animation(times22, 90, 8, "WHouse/WH-j-med/WH-j-med"), Cond1, 18, 8, 14, Action.AIR, 3, true, true, false, false);
  j_MidNormal.jumpCancel = true; j_MidNormal.dashCancel = true;
  
    int[] times21 = {3, 3, 2, 3, 2, 3, 2, 3, 2, 2};
  Animation Anim21 = new Animation( times21, 50, 50, 10, "WHouse/WH-j-h/WH-j-h");
  j_HeavyNormal = new Action("WH-j-h", Anim21, Cond2, 14, 7, 20, Action.AIR, 4, true, true, false, false);
  j_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.WALLBOUNCE, new WH_gearCheck(2, this)),
  new CounterEff(Action.WALLBOUNCE, new CounterHit())};
  
  Action j_DustNormalp2 = new Action("Wh-j-tp2", new Animation( new int[]{2, 6, 2, 2, 2, 2}, 50, 6, "WHouse/WH-j-t/p2/WH-j-t"), 
  new Condition[]{}, 0, 0, 0, Action.MID, 7, true, false, false, false);
  
    j_DustNormal = new Action("Wh-j-t", new Animation( new int[]{3, 3, 3, 2, 40}, 50, 5, "WHouse/WH-j-t/WH-j-t"), 
  new Condition[]{new fPButCond(8)}, 20, 10, 9, Action.HIGH, 7, true, false, false, false);
  j_DustNormal.ActEffs = new ActEffect[]{ new GravEff(0, 0, true), new GravEff(2, 0, false), new AddOwnForcToOpp(2, 1), new ChangeActTo(j_DustNormalp2, 1, new Grounded() ) };
  
  int[] times24 = {3, 3, 3, 4, 3, 4, 2, 2, 2, 4, 4, 2};
  Animation Anim24 = new Animation( times24, 50, 0, 12, "WHouse/WH-j-heay/WH-j-heay");
  Special6drill = new Action("Wh-j-heay", Anim24, new Condition[]{new WH_gearCheck(3, this), new InAir(), new fPButCond(4), new comfPButC(new int[][]{ {2, 3,  6} }) },
  16, 10, 7, Action.AIR, 20, false, true, false, true);
  Special6drill.gravMult = 0.70f;
  //Special6drill.ActEffs = new ActEffect[]{ new AddOwnForcToOpp( 2, 1, new Condition() )};
           Action FillAct = Special6drill;
   FillAct.updFrameDataArr_float(0, 22, 0);
   
   /*int[] times25 = {4, 3, 6, 2, 3, 6, 3, 3, 3};
   st_fHeavy = new Action("FH-st-forH",//"data/FH-st-fHeavy", 
   new Animation( times25, 75, 9, "FHouse/FH-st.fH/FH-st-fH"), 18, 14, 20, Action.HIGH, true, false, false, false);*/
   
         int[] times26 = {3, 3, 3, 2, 2, 3, 6, 4, 2, 2, 3, 2, 2};
      Condition[] Cond3 = {new Grounded(), new fPButCond(5), new VertForceCheck(5.0f)};
   Special1 = new Action(//"FH-st-forH",
   new Animation( times26, 0, 13, "WHouse/WH-sp1/WH-1sp"), 18, 6, 10, Action.HIGH, true, true, true, false);
   Special1.Conds = Cond3; Special1.attWeight = 20; 
   Special1.ActEffs = new ActEffect[]{new ChangeAnimCoords(0, 0, new ActTimeCond2(0, 1) ), new ChangeAnimCoords(0, 200, new ActTimeCond2(3, 1) ), new WH_stance(this, 1, new ActTimeCond2(5, 2) ), 
   new ForceAddEff(0, 0, true), new ForceAddEff(1, 4, false)
 };
   Special1.hitCancel = false;
   FillAct = Special1;
   FillAct.updFrameDataArr(0, 6);
   FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   FillAct.addAllLists(1, 4, 6, 0);
   FillAct.addAllLists(2, 1, 24, -42);

   FillAct.addAllLists(3, 15, 10, -20);
   FillAct.HurtBoxCollect.get(3).add(new ColCircle(0, 0, 330, 100));
      FillAct.HitBoxCollect.get(3).add(new ColCircle(30, 0, 360, 120));
   FillAct.addAllLists(4, 7, 0, 0);
      FillAct.HurtBoxCollect.get(4).add(new ColCircle(0, 0, 330, 100));
   //FillAct.HitBoxCollect.get(1).add(new ColCircle(30, 0, 360, 120));
   FillAct.addAllLists(5, 4, 0, 0);
   
   int[] revFB = {2, 1, 4}, FB = {2, 3, 6};
         int[] times28 = {3, 3, 2, 2, 2, 4, 2, 2, 2, 2};
         Condition[] Cond4 = {new fPButCond(6), new dirCombCond(revFB) /*new VertForceCheck(-4.0)*/};
   Special2 = new Action("Wh-sp2",
   new Animation( times28, 100, 10, "WHouse/WH-sp2/WH-2sp"), Cond4, 32, 10, 4, Action.MID, 18, true, true, false, false);
   Special2.hitCancel = false; Special2.ActEffs = new ActEffect[]{new CounterEff(Action.STAGGER)};
   
         int[] times29 = {4, 4, 3, 8, 4, 4, 2, 3};
         Condition[] Cond5 = {new WH_checkStance(this, false), new WH_gearCheck(1, this), new Grounded(), new fPButCond(4), new dirCombCond(FB)};
   Special3rek1 = new Action("Wh-sp3-rek1",
   new Animation( times29, 65, 8, "WHouse/WH-sp3-rekka1/WH-4sp-1rekka"), Cond5, 22, 10, 14, Action.MID, 18, true, false, false, false);
   Special3rek1.dashCancel = true;
   
            int[] times30 = {5, 4, 3, 8, 6, 4, 3, 2};
         Condition[] Cond6 = {new WH_checkStance(this, false), new WH_gearCheck(1, this), new Grounded(),
       new CheckStateCond(Special3rek1), new CheckAttPart(2), new fPButCond(4), new comfPButC(new int[][]{FB, {2, 6}, {3, 6} } )};
   Special3rek2 = new Action("Wh-sp3-rek2",
   new Animation( times30, 65, 8, "WHouse/WH-sp3-rekka2/WH-4sp-2rekka"), Cond6, 22, 10, 14, Action.MID, 26, true, false, false, false);
   Special3rek2.ActEffs = new ActEffect[]{new CounterEff(2, new Condition()), 
   new ChangeActTo(Special1, 1, new CheckAttPart(2), new fPButCond(5), new comfPButC(new int[][]{FB, {2, 6}, {3, 6} } ), new WH_gearCheck(2, this) ) };
   Special3rek2.jumpCancel = true; Special3rek2.dashCancel = true;
   
               int[] times31 = {4, 3, 2, 4, 6, 4, 2, 3};
         Condition[] Cond7 = {new WH_checkStance(this, false), new Grounded(), new WH_gearCheck(2, this), new fPButCond(6), new dirCombCond(FB), new OrCond( 
      new Condition[]{},
      new Condition[]{new CheckStateCond(Special3rek1), new CheckAttPart(2)}
   )  
   };
   Special4 = new Action("Wh-sp4-slide",
   new Animation( times31, 65, 8, "WHouse/WH-sp4-slide/WH-4sp-slide"), Cond7, 24, 10, 4, Action.LOW, 26, true, true, true, false);
   
   OilProj = new WH_Oilpuddle(0.0f, 0.0f, 200, 0, 200, 100, 0.0f, 0.0f, 1, 600, 0, 0, 0, false, false, false, false, false) ;
   OilProj.setAnims(new Animation(new int[]{10}, 0, -25, 1, "Projectiles/Oilpuddle/Oilpuddle"), new Animation(new int[]{10}, 0, -25, 1, "Projectiles/Oilpuddle/Oilpuddle"));
                  int[] times32 = {2, 6, 2, 6, 2,
                2, 2, 6, 2, 2,
              2, 3, 2};
         Condition[] Cond8 = {new fPButCond(5), new dirCombCond(revFB)};
   Special5 = new Action("Wh-sp2",
   new Animation( times32, 100, 10, "WHouse/WH-sp5-oil/WH-5sp-oil"), Cond8, 32, 10, 4, Action.MID, 18, true, false, false, false);
   Special5.ActEffs = new ActEffect[]{ new ProjAddEff( OilProj, 1, new ActTimeCond2(3, 2) )};
   
   SpeedProj = new Projectile(0,0, 200, -100, 70, 100, 10, 0, 0.0f, 80, 30, 20, 8, false, false, false, true, true) ;
   SpeedProj.setAnims(new Animation(new int[]{3, 4, 6, 6, 6}, 0, -25, 5, "Projectiles/speedproj/speedproj"), new Animation(new int[]{3,3,3,3}, 0, -25, 4, "Projectiles/speedproj/p3/speedproj"));

   Sp7speedProj = new Action("Wh-sp7",
   new Animation( new int[]{3, 2, 3, 3, 10,  4, 3, 3, 10, 4,  4, 4}, 115, 12, "WHouse/WH-sp-speedproj/WH-sp-speedproj"),
   new Condition[]{new WH_gearCheck(1, this), new fPButCond(5), new dirCombCond(FB)},
   10, 10, 4, Action.MID, 18, true, false, false, false);
   Sp7speedProj.ActEffs = new ActEffect[]{ new ProjAddEff( SpeedProj, 1, new FirstHitCheck(), new ActTimeCond2(4, 1) ), 
   new WH_gearAdd(-1, this, 1, new ActTimeCond2(0, 2) )};
   
   Sp8speedDP = new Action("Wh-sp8",
   new Animation( new int[]{2, 3, 5, 3, 2, 5, 5, 3, 3, 4, 3}, 25, 11, "WHouse/WH-sp-speedDP/speedDP"),
   new Condition[]{new Grounded(), new WH_gearCheck(1, this), new fPButCond(5), new comfPButC( new int[][]{{6, 2, 3}, {6, 3, 2, 3, 6}, {6, 3, 2, 3}, {6, 2, 6}})},
   20, 10, 12, Action.MID, 18, true, false, false, false);
   Sp8speedDP.ActEffs = new ActEffect[]{new FirstHitEff(1, 0, true), new FirstHitEff(2, 1, true), new FirstHitEff(3, 1, true),// new ProjAddEff( SpeedProj, 1, new FirstHitCheck(), new ActTimeCond2(4, 1) ), 
   new WH_gearAdd(-1, this, 1, new ActTimeCond2(0, 2) )};
   
                     int[] times33 = {2, 4, 4, 4, 4, 2, 4, 4, 4, 4, 2, 4, 4};
   Super1RTL = new Action("Wh-1super", new Animation( times33,0, 100, 13, "WHouse/WH-1superRTL/WH-1superRTL"), 32, 18, 4, Action.MID, false, false, false, true);
      int[][] superm = {{2, 3, 6, 2, 3, 6}, {2, 3, 6, 2, 3}, {2, 6, 2, 6}, {2, 3, 6, 2, 3, 6, 9}};
       Super1RTL.Conds = new Condition[]{new SuperCond(500), new comfPButC(superm ), new fPButCond(5)};
       Super1RTL.ActEffs = new ActEffect[]{ new AddOwnForcToOpp(2, 1) }; 
       Super1RTL.superNeed = 500; Super1RTL.attWeight = 40;
       
  StanceFDash = new Action("WH-stance-fdash", 
  stanceFWalk.AttAnim,//new Animation(new int[]{2, 4, 3, 3}, 0, 4, "WHouse/WH-stance-light/WH-stance-l"),
  new Condition[]{new Grounded(), new comfPButC(new int[][]{{6, 6}} )}, 6, 4, 5, Action.MID, 10, true, false, false, false);
  StanceFDash.collision = false;    
  
    int[] etimes1 = {2, 4, 3, 3};
  StanceLight = new Action("WH-stance-l", 
  new Animation(etimes1, 0, 4, "WHouse/WH-stance-light/WH-stance-l"), new Condition[]{new Grounded(), new fPButCond(4)}, 6, 4, 5, Action.LOW, 10, true, false, false, false);
  StanceLight.hitCancel = false;
  
    int[] etimes2 = {3, 3, 3};
  StanceMed = new DirChangeAct("WH-stance-m", 
  new Animation(etimes2, 0, 3,"WHouse/WH-stance-Med/WH-stance-m"), new Condition[]{new Grounded(), new fPButCond(6)}, 4, 1, 10, Action.MID, 11, true, true, false, false);
  StanceMed.hitCancel = false;
  
    int[] etimes3 = {3, 3, 3, 3, 6};
  StanceHeavy = new Action("WH-stance-heay", 
  new Animation(etimes3, 0, 5,"WHouse/WH-stance-heavy/WH-stance-h"), new Condition[]{new Grounded(), new fPButCond(5)}, 14, 12, 20, Action.HIGH, 12, true, true, false, true);
  StanceHeavy.hitCancel = false;
  
        Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, Special1, Special2, Special3rek1, Special3rek2, Special4, Special5, Sp8speedDP, Sp7speedProj, Super1RTL, NormalThrow},
  {cr_LightNormal, cr_MidNormal2, cr_HeavyNormal, cr_MidNormal, Super1RTL},
  {j_LightNormal, j_MidNormal, j_HeavyNormal, j_DustNormal, AirThrow, Special6drill, Super1RTL},
  {Special3rek2, Special3rek1, Special4, Special6drill, Sp8speedDP, Sp7speedProj, Super1RTL},
  {StanceLight, StanceMed, StanceHeavy, StanceFDash, Special1}
};
  fillActionsList(ActTab);
}

  public void drawBars(int mult){
    super.drawBars(mult);
    for(int i = 0; i < 3; i++){
      fill(50, 200, 10);
      if(i+1 <= gearLevel) fill(250, 200, 10);
      rect(PApplet.parseInt(Camerabox.x) - mult*(initWidth/4 + 22*i), GROUNDHEIGHT-10 , 20, 20);
    }
    text( Force.x + ":KM/H", PApplet.parseInt(Camerabox.x) - mult*(initWidth/4), GROUNDHEIGHT-10 );
  }

  public void standingStateReturn(Fighter Opp){
    if(stance){
      CollisionBox.ho = 100;
      CurAction.playAction2(this, Opp, Stance);
    }
    else if(!stance){
      super.standingStateReturn(Opp);
        CollisionBox.ho = 180;
      }
    
      if(y >= GROUNDHEIGHT || CollisionBox.bottom){
        curAirActions = maxAirActions;
      }
          if(CurAction == InHitStun || CurAction == softKD || CurAction == Knockdown){
      stance = false;
    }

  }
  
  public void fighterActions(Fighter Opp){
    
    if(!stance)super.fighterActions(Opp);
    fighterActionsExtra();
  }
  
  public void fighterActionsExtra(){
     Force.x = constrain(Force.x, -100, 100);
     float l_fx = abs(Force.x);
     if(CurAction.attRank != Action.HITSTATE){
     if(l_fx >= 30 && gearLevel <= 3) gearLevel = 3;
     else if(l_fx >= 20 && gearLevel <= 2) gearLevel = 2;
     else if(l_fx >= 10 && gearLevel <= 1) gearLevel = 1;
     }
     if(l_fx >= 20) gearTime = 0;
     
     gearTime++; 
     if(gearTime >= 800){ 
     gearTime = 0;
     if(gearLevel > 0) gearLevel--;
   }
   
   if(gearLevel >= 3){
     if(curSuper <= maxSuper) curSuper++;
     if(!stance && new comfPButC(new int[][]{{2, 2}} ).cond(this, this)){
       stance = true;
       inputbufferDir.set(inputbufferDir.size()-1, 5);
     }
 }
   
   if(gearLevel >= 3){FDash = fDash_gl3; BDash = bDash_gl3;} else {FDash = fDash_gl0; BDash = bDash_gl0;}
    
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
        
    checkSingleActList(this, ActionList.get(4));
    
         int[] Stmotion = {2, 2}; 
    if( compareBufferWithCombAtt(Stmotion)){
      stance = false;
      //changeAction( Standing);
           inputbufferDir.set(inputbufferDir.size()-1, 5);
         }
         
    }
       
  }
  
  public void st_Normals(){
/*
   if(inputs[5] && firstPressInp[5] && (Force.x >= 6 || Force.x <= -6) && CurAction.attWeight < Special1.attWeight){
          stance = true;
      changeAction(Special1);    
    }*/

  }
  
  public void cr_Normals(){
  }
  
  public void j_Normals(){
  }
 
  
}

//Plumsklo###############################################################################################################################################################################################
class F_PHaus extends Fighter{
  int curFartMeter = 0, maxFartMeter = 90;
  
  Action st_fHeavy, j_MidNormal2;
  Action BaseNormal, RoofNormal;
  Action Special1, Special3l1, Special3l2, Special3l3, Special4l1, Special4l2, Special4l3, Special5, Special6, Super1;
  PHaus_Sp2 Special2;
  Animation FB1Anim, FB1DestrEff, FartEff0, FartEff1, FartEff2;
  
    Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, st_fHeavy, BaseNormal, RoofNormal, FDash, BDash, Jumping, fDiaJump, bDiaJump, Super1},
  {cr_LightNormal, cr_MidNormal, cr_HeavyNormal, Super1},
  {j_LightNormal, j_MidNormal, j_MidNormal2, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2},
  {Special1,Special2, Special3l1, Special3l2, Special3l3, Special4l1, Special4l2, Special4l3, Super1}
};
  
  F_PHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
        int[] times0 = {60, 4, 4, 4, 4, 4, 4};
      Animation Anim0 = new Animation(times0, 0, 7, "PHouse/PH-st/PH-st");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_PHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
    F_PHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
    F_PHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
 
  F_PHaus(int x, int y, PlControl PlContr){
    super(x, y, PlContr);
  }
  
public void specSetup(){
   CollisionBox = new ColRect(0, 0, 150, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
    int[] etimes0 = {10, 10, 10, 10};
    int[] etimes1 = {3, 3, 5, 5, 3, 3, 3, 3};
  FB1Anim = new Animation(etimes0, 0, 4, "Projectiles/Apple/PH-projApple");
  FB1DestrEff = new Animation(etimes1, 0, 0, 8, "Effekte/BettDestrEff/FH-BettEff");
  FartEff0 = new Animation(new int[]{2, 2, 2, 2, 2, 2, 2} , 0, 0, 7, "Effekte/PhEff/PH-1spFartEff");
  FartEff1 = new Animation(new int[]{2, 2, 2, 2, 2, 2, 2}, 0, 0, 7, "Effekte/PhEff/PH-2spFartEff");
  FartEff2 = new Animation(new int[]{2, 2, 2, 2, 2, 2, 2, 2}, 0, 0, 8, "Effekte/PhEff/PH-3spFartEff");
  
  int[] times0 = {60, 4, 4, 4, 4, 4, 4};
  Animation Anim0 = new Animation(times0, 0, 7, "PHouse/PH-st/PH-st");
      Standing = new Action("FH-st-idle", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
      
  Falling.AttAnim = new Animation(new int[]{4}, 0, 90, 1, "PHouse/PH-jumpfall/PH-jfall");
  Landing.AttAnim = new Animation(new int[]{3}, 0, 1, "PHouse/PH-landing/PH-landing");      
  Turning.AttAnim = new Animation(new int[]{3,3,3},  0, 3, "PHouse/PH-turning/PH-turning");
        
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, 0, 6, "PHouse/PH-fWalk/PH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 4, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2.8f, 0);
  
  int[] times3 = {2, 1, 2, 1, 1};
  Animation Anim3 = new Animation(times3, 0, 90, 5, "PHouse/PH-njump/PH-njump");
  Anim3.loop = false;
  Jumping = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  Jumping.resetAnim = true;

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  Jumping.addAllLists(1, 2, 0, -20);
  
  fDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  fDiaJump.resetAnim = true;

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
      fDiaJump.addAllLists(1, 2, 11, -20);
  
  bDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  bDiaJump.resetAnim = true;

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bDiaJump.addAllLists(1, 2, -7.5f, -20);
  
  Jumping2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  
  FDash = new Action(new Animation(new int[]{2, 3, 5, 2}, 0, 4, "PHouse/PH-dash/PH-fdash"),
  10, 5, 3, Action.MID, false, false, false, false);
  
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 8, 0);
    FDash.addAllLists(1, 8, 10, -4);
    
  BDash = new Action(new Animation(new int[]{4, 4, 2}, 0, 3, "PHouse/PH-dash/PH-bdash"), 
  10, 5, 3, Action.MID, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, 0, 0);
  BDash.addAllLists(1, 5, -8, 0);
  
    airFDash = new Action(new Animation(new int[]{2, 3, 5, 2}, 0, 4, "PHouse/PH-dash/PH-fdash"),
  10, 5, 3, Action.MID, false, false, false, false);
  airFDash.allButSelfCancel = true;
    airFDash.updFrameDataArr(0, 5);
    airFDash.updFrameDataArr_float(0, 8, 0);
    airFDash.addAllLists(1, 8, 18, 0);
    
  airBDash = new Action(new Animation(new int[]{4, 4, 2}, 0, 3, "PHouse/PH-dash/PH-bdash"), 
  10, 5, 3, Action.MID, false, false, false, false);
  
  airBDash.updFrameDataArr(0, 5);
  airBDash.updFrameDataArr_float(0, 0, 0);
  airBDash.addAllLists(1, 5, -12, 0);
  
    int[] times2 = {5};
  Crouching = new Action("ph-cr", new Animation(times2, 0, 1, "PHouse/PH-cr/PH-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "PHouse/PH-Stun/PH-HHitSt"); LHit = new Animation(times1, 0, 1, "PHouse/PH-Stun/PH-LHitSt");
  HBlock = new Animation(times1, 0, 1, "PHouse/PH-Stun/PH-HBlockSt"); LBlock = new Animation(times1, 0, 1, "PHouse/PH-Stun/PH-LBlockSt");
  
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
  
  /*int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD");
  //softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-hKD/FH-hKD");*/

    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  
    int[] times14 = {2, 2, 5, 2, 2};
  Condition[] Cond1 = {new fPButCond(4)};    
  Animation Anim14 = new Animation(times14, 20, 5, "PHouse/PH-st-light/PH-st-light");
  LightNormal = new Action("PH-st-light", Anim14, Cond1, 10, 5, 3, Action.MID, 1, true, false, false, false);
  LightNormal.ActEffs = new ActEffect[]{new SoundEff(6, 0, 0), new CounterEff(1, new CounterHit() )}; //GEFAHR

  Condition[] Cond2 = {new fPButCond(6)};
    int[] times16 = {4, 4, 4, 4, 4, 4};
  Animation Anim16 = new Animation(times16, 75, 6,"PHouse/PH-st-med/PH-st-med");
  MidNormal2 = new Action("Ph-st-med", Anim16, Cond2, 18, 10, 15, 3, 3, true, false, false, false);
  
  Condition[] Cond3 = {new fPButCond(5)};
    int[] times17 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};
    Animation Anim17 = new Animation(times17, 85, 11,"PHouse/PH-st-heay/PH-st-heay");
  HeavyNormal = new Action("Ph-st-heay", Anim17, Cond3, 24, 16, 20, Action.MID, 5, true, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.STAGGER, new CounterHit() )};
  
  int[] times18 = {2, 2, 2, 4, 2};
  Animation Anim18 = new Animation(times18, 25, 5, "PHouse/PH-cr-light/PH-cr-light");
  cr_LightNormal = new Action("Ph-cr-light", Anim18, Cond1, 9, 5, 3, 3, 2, true, false, false, false);

  int[] times19 = {6, 4, 4, 4, 4, 4, 4, 4};
  Animation Anim19 = new Animation(times19, 45, 8, "PHouse/PH-cr-med/PH-cr-med");
  cr_MidNormal = new Action("Ph-cr-med", Anim19, new Condition[]{new ButCond(1), new fPButCond(8)}, 60, 10, 10, Action.LOW, 10, true, true, true, true);
  cr_MidNormal.ActEffs = new ActEffect[]{new KDownEff(0, 0, false), new KDownEff(2, 1, true)};
  
  cr_MidNormal2 = new Action("Ph-cr-m2", new Animation(new int[]{4, 4, 2, 4, 3, 3, 3}, 55, 7, "PHouse/PH-cr-m2/PH-cr-2m"),
  Cond2, 20, 14, 10, Action.MID, 4, true, false, false, false);
  
    int[] times23 = {4, 4, 4, 4, 2, 4, 6, 6, 4, 2}; //40
  cr_HeavyNormal = new Action( "Ph-cr-heay", new Animation(times23, 25, 10, "PHouse/PH-cr-heay/PH-cr-heay"), Cond3,
                               16, 8, 24, Action.MID, 10, true, false, false, false);
  cr_HeavyNormal.jumpCancel = true;
  cr_HeavyNormal.ActEffs = new ActEffect[]{new CounterEff(Action.JUGGLE, new CounterHit())};
  
  int[] times20 = {2, 1, 4, 2, 1};
  Animation Anim20 = new Animation(times20, 25, 90, 5, "PHouse/PH-j-light/PH-j-light");
  j_LightNormal = new Action("Ph-j-light", Anim20, Cond1, 9, 4, 3, Action.AIR, 1, true, true, false, false);
  j_LightNormal.jumpCancel = true; j_LightNormal.dashCancel = true;
  
  int[] times22 = {2, 2, 2, 3, 4, 3, 2};
  j_MidNormal2 = new Action( "PH-j-med", new Animation(times22, 65, 90, 7, "PHouse/PH-j-med/PH-j-med"),
            Cond2, 12, 7, 10, Action.AIR, 4, true, true, false, false);
            j_MidNormal2.jumpCancel = true; j_MidNormal2.dashCancel = true;
  j_MidNormal2.ActEffs = new ActEffect[]{new CounterEff(Action.GROUNDBOUNCE, new CounterHit() )};
  
  int[] times21 = {2, 4, 3, 2, 4, 3, 2, 3};
  Animation Anim21 = new Animation( times21, 95, 110, 8, "PHouse/PH-j-heavy/PH-j-heavy");
  j_HeavyNormal = new Action("Ph-j-heay", Anim21, Cond3, 18, 8, 20, Action.AIR, 4, true, true, false, false);
  j_HeavyNormal.jumpCancel = true; j_HeavyNormal.dashCancel = true;
  
  int[] times24 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4};
  int[][] DPvars = {{2, 1, 4}};
  Condition[] Cond4 = {new comfPButC(DPvars), new fPButCond(4)};
  Special1 = new PH_sp1("PH-special1", new Animation( times24, 0, 10, "PHouse/PH-special1/PH-1special"), Cond4, 18, 16, 6, Action.MID, 20, true, true, true, true, this);
  int[] times30 = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
  Condition[] Cond30 = {new comfPButC(DPvars), new fPButCond(6)};
  Special2 = new PHaus_Sp2("PH-special2", new Animation( times30, 0, 10, "PHouse/PH-special2/PH-2special"), Cond30, 6, 2, 6, Action.MID, 20, true, true, true, true,
  1.5f, 400, 0, 0, true, true, false, this);
  Special2.ProjAnim = FB1Anim;
  Special2.destrEffAnim = FB1DestrEff;
  Special2.gravMult = 0.5f;
   
  Condition[] Cond5 = {new facingCond(1), new fPButCond(5)};
   int[] times25 = {4, 3, 6, 2, 3, 6, 3, 3, 3};
   st_fHeavy = new Action("FH-st-forH",//"data/FH-st-fHeavy", 
   new Animation( times25, 75, 9, "FHouse/FH-st.fH/FH-st-fH"), Cond5, 18, 14, 6, Action.HIGH, 12, true, false, false, false);
      
    int[] times21p2 = {2, 2, 2, 2};
    Animation Sp3p2Anim = new Animation(times21p2, 70, 100, 4,"PHouse/PH-special3/PH-3special1p");
    Action Special3p2 = new Action(// "FH-sp3p2",
    Sp3p2Anim, 0, 0, 0, Action.MID, true, true, true, false);
    
         Action FillAct = Special3p2;
    FillAct.updFrameDataArr(0, 8);
  //FillAct.updFrameDataArr_float(0, 16, 18);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -100, 200, 200));
    
    int[] times21p1 = {2, 2, 2, 2, 4, 2, 4, 2, 4, 2};
    Animation Sp3p1Anim = new Animation(times21p1, 70, 100, 10,"PHouse/PH-special3/PH-3special1p");
    
    int[][] revFBvars = {{2, 3, 6}, {2, 3, 6, 7}};
    Condition[] Cond6 = {new Grounded(), new comfPButC(revFBvars), new fPButCond(5), new PH_FartCond(0, 5, this)};
    Special3l1 = new ChangeOnEndAct( "Ph-special3-2",
    Sp3p1Anim, 12, 8, 12, Action.MID, true, true, false, false, Special3p2); 
    Special3l1.Conds = Cond6; Special3l1.attWeight = 30;
          FillAct = Special3l1; FillAct.updFrameDataArr_float(1, 3, -15);
         Special3l1.ActEffs = new ActEffect[]{ new AddVisEff(FartEff1, 0, -120, 1, new ActTimeCond2(0, 0) ) };
   
   Condition[] Cond6_2 = {new Grounded(), new comfPButC(revFBvars), new fPButCond(5), new PH_FartCond(30, 15, this)};
    Special3l2 = new ChangeOnEndAct( "Ph-special3-2",
    Sp3p1Anim, 20, 14, 20, Action.MID, true, true, true, false, Special3p2); 
    Special3l2.Conds = Cond6_2; Special3l2.attWeight = 30;
          FillAct = Special3l2; FillAct.updFrameDataArr_float(1, 4.4f, -22);
          Special3l2.ActEffs = new ActEffect[]{ new AddVisEff(FartEff1, 0, -120, 1, new ActTimeCond2(0, 0) ) };
   
      Condition[] Cond6_3 = {new Grounded(), new comfPButC(revFBvars), new fPButCond(5), new PH_FartCond(60, 20, this)};
    Special3l3 = new ChangeOnEndAct( "Ph-special3",
    Sp3p1Anim, 20, 18, 12, Action.MID, true, true, true, true, Special3p2); 
    Special3l3.Conds = Cond6_3; Special3l3.attWeight = 30;
          FillAct = Special3l3; FillAct.updFrameDataArr_float(1, 5.6f, -25);
          Special3l3.ActEffs = new ActEffect[]{ new AddVisEff(FartEff1, 0, -120, 1, new ActTimeCond(0, 0, 1,0) ) };
   
       int[] times26p2 = {2, 2, 2, 4, 2};
    Animation Sp4p2Anim = new Animation(times26p2, 0, 5,"PHouse/PH-special4/PH-4special2p");
    Action Special4p2 = new Action(// "FH-sp3p2",
    Sp4p2Anim, 0, 0, 0, Action.MID, true, false, true, false);
    Sp4p2Anim.loop = false;
    
         FillAct = Special4p2;
    FillAct.updFrameDataArr(0, 12);
   FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -100, 200, 200));
    
    int[] times26p1 = {3, 4, 2, 3, 60};
    Animation Sp4p1Anim = new Animation(times26p1, 0, 5,"PHouse/PH-special4/PH-4special1p"); Sp4p1Anim.loop = false;
    
       int[][] halfCircM = {{4, 1, 2, 3, 6}, {4, 2, 6}, {4, 1, 2, 3, 6, 7}, {2,3,6}};
   Condition[] Cond7_1 = {new comfPButC(halfCircM), new fPButCond(6), new PH_FartCond(0, 5, this)};
  Special4l1 = new ChgOEndAndHit("PH-special4l1", Sp4p1Anim, Cond7_1, 10, 8, 10, Action.MID, 20, true, false, false, false, Special4p2);
            FillAct = Special4l1; FillAct.updFrameDataArr_float(1, 16, 0);
            Special4l1.ActEffs = new ActEffect[]{ new AddVisEff(FartEff0, -120, -110, 1, new ActTimeCond2(0, 0) ) };
   
      Condition[] Cond7_2 = {new comfPButC(halfCircM), new fPButCond(6), new PH_FartCond(30, 8, this)};
  Special4l2 = new ChgOEndAndHit("PH-special4l2", Sp4p1Anim, Cond7_2, 18, 14, 14, Action.MID, 20, true, false, false, false, Special4p2);
            FillAct = Special4l2; FillAct.updFrameDataArr_float(1, 20, 0);
            Special4l2.ActEffs = new ActEffect[]{ new AddVisEff(FartEff0, -120, -110, 1, new ActTimeCond2(0, 0) ) };
            
               Condition[] Cond7_3 = {new comfPButC(halfCircM), new fPButCond(6), new PH_FartCond(60, 14, this)};
  Special4l3 = new ChgOEndAndHit("PH-special4l3", Sp4p1Anim, Cond7_3, 25, 23, 18, Action.MID, 20, true, false, false, false, Special4p2);
            FillAct = Special4l3; FillAct.updFrameDataArr_float(1, 28, 0);
            Special4l3.ActEffs = new ActEffect[]{new CounterEff(1, new Condition[]{new CounterHit()})}; //GEFAHR
            Special4l3.ActEffs = new ActEffect[]{ new AddVisEff(FartEff0, -120, -110, 1, new ActTimeCond2(0, 0) ) };
            
      int[] times1p3 = {2, 2, 2, 2};
    Action Special5p3 = new Action( //"HH-cr-Light",
    new Animation(times1p3, 0, 100, 4, "PHouse/PH-special5/PH-5sp3p"), 6, 5, 4, Action.AIR, true, false, false, false);
    Special5p3.gravMult = 0.8f;
    FillAct = Special5p3;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
    FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -90, 180, 180));
    
    int[] times1p2 = {2, 2, 2, 2, 2, 2, 2, 2};
    Action Special5p2 = new HoldButToKeepAct( "Ph-special5p2",
    new Animation(times1p2, 0, 100, 8, "PHouse/PH-special5/PH-5sp2p"), 6, 5, 4, Action.AIR, true, true, false, false, Special5p3, 40, 0);
    Special5p2.gravMult = 0.7f;
    
    int[] times1p1 = {2, 2, 2, 2};
    Special5 = new ChangeOnEndAct( //"HH-cr-Light",
    new Animation(times1p1, 0, 100, 4, "PHouse/PH-special5/PH-5sp1p"), 6, 5, 4, Action.AIR, true, true, false, false, Special5p2); 
    Special5.Conds = new Condition[]{new InAir(),  new fPButCond(0), new PH_FartCond(15, 15, this)}; Special5.attWeight = 3; Special5.gravMult = 0.85f;
          FillAct = Special5;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, -20);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -90, 180, 180));
   
     int[] times26 = {4, 2, 4, 2, 3, 2, 2, 3, 2, 2, 2, 2};
  int[][] FB = {{2, 3, 6}}, revFB = {{2, 1, 4}};
  // new UseEffCond(Special6, new Condition[]{ new comfPButC(FB), new fPButCond(5) }, new ActEffect[]{new PH_dirEff(1, new Condition[]{}, -20, -20, 180, -1)} 
  Special6 = new Action("PH-special6", new Animation( times26, 0, 100, 12, "PHouse/PH-special6/PH-6sp"), new Condition[]{}, 22, 16, 6, Action.AIR, 30, false, false, false, false);
           Special6.AttAnim.rot = 0; FillAct = Special6;
    FillAct.updFrameDataArr_float(2, 30, 0);
      Special6.Conds = new Condition[]{new InAir(),
      
      new OrCond(
            new OrCond( 
    new UseEffCond(Special6, new Condition[]{ new comfPButC(FB), new fPButCond(5) }, new ActEffect[]{ new AddVisEff(FartEff2, -120, -100, 1, new ActTimeCond2(0, 0) ), new PH_dirEff(1, new Condition[]{}, 22, -10, TWO_PI-PI/8, -1), new AddOwnForcToOpp(2,1, new Condition())}),
    new UseEffCond(Special6, new Condition[]{ new comfPButC(FB), new fPButCond(4) }, new ActEffect[]{ new AddVisEff(FartEff2, -120, -100, 1, new ActTimeCond2(0, 0) ), new PH_dirEff(1, new Condition[]{}, 30, 0, 0, -1), new AddOwnForcToOpp(2,1.4f, new Condition())})
    ),
            new OrCond( 
    new UseEffCond(Special6, new Condition[]{ new comfPButC(revFB), new fPButCond(5) }, new ActEffect[]{ new AddVisEff(FartEff2, -120, -100, 1, new ActTimeCond2(0, 0) ), new PH_dirEff(1, new Condition[]{}, 22, 10, PI/8, -1), new AddOwnForcToOpp(2,1, new Condition()) }),
    new UseEffCond(Special6, new Condition[]{ new comfPButC(revFB), new fPButCond(4) }, new ActEffect[]{ new AddVisEff(FartEff2, 200, -100, 1, new ActTimeCond2(0, 0) ), new PH_dirEff(1, new Condition[]{}, -30, 0, PI, -1), new AddOwnForcToOpp(2,1, new Condition()) })
    )
    ),     
 new PH_FartCond(15, 15, this)}; //needed for dirAff: fx, fy, dirMult, hurt- and hitboxen
 Special6.selfCancel = true;
                   
   int[][] superm = {{2, 3, 6, 2, 3, 6}, {2, 3, 6, 2, 3}, {2, 6, 2, 6}, {2, 3, 6, 2, 3, 6, 9}};
       Condition[] Cond8 = {new SuperCond(500), new comfPButC(superm), new fPButCond(6), new PH_FartCond(0, 30, this)};
     int[] times27= {3, 3, 3, 3, 3,
   3, 3, 3, 3, 3,
 3, 3, 3, 3, 3, 3};
  Super1 = new Action("FH-1super", new Animation( times27, 95, 16, "FHouse/FH-1super/FH-1super"), Cond8, 20, 18, 10, Action.MID, 50, true, false, false, true);
   Super1.superNeed = 500;
   
    Action[][] ActTab = {
  {LightNormal, MidNormal2, HeavyNormal, st_fHeavy, FDash, BDash, Jumping, fDiaJump, bDiaJump, Special1, Special2, 
   Special3l3, Special3l2, Special3l1, Special4l3, Special4l2, Special4l1, Super1, NormalThrow},
  {cr_LightNormal, cr_MidNormal2, cr_MidNormal, cr_HeavyNormal, AirThrow, Special1, Super1},
  {j_LightNormal,j_MidNormal2, j_HeavyNormal, FDash, BDash, Jumping2, fDiaJump2, bDiaJump2, Special2, Special5, Special6},
  {Special1, Special2, Special3l3, Special3l2, Special3l1, Special4l3, Special4l2, Special4l1, Super1, Special6}
};

  fillActionsList(ActTab);
   
}

  public void drawBars(int mult){
    float centerZ2 = 0 / tan(PI*30.0f / 180.0f);
    drawBar( PApplet.parseInt(Camerabox.x) - mult*(initWidth/4), 20 - PApplet.parseInt(0) , centerZ2, maxHP, curHP, mult * -180);
    drawBar( PApplet.parseInt(Camerabox.x) - mult*(initWidth/4), GROUNDHEIGHT+10 - PApplet.parseInt(0) , centerZ2, maxSuper, curSuper, mult * -140);
    drawBar( PApplet.parseInt(Camerabox.x) - mult*(initWidth/4), GROUNDHEIGHT-10 - PApplet.parseInt(0) , centerZ2, maxFartMeter, curFartMeter, mult * -90);
  } 

  public void st_Normals(){
  }
  public void cr_Normals(){
  }
  public void j_Normals(){
    }   
   
  public void fighterActionsExtra(){
    curFartMeter = constrain(curFartMeter, 0, maxFartMeter);
  }
  
    // "0000 (Dir) 000 (AttButs) 0 (RC) : timeactive"
  
  InputRecord FBl = new InputRecord(this, 2, true, 
  "0100 000 0 : 3",
  "0110 000 0 : 3",
  "0010 000 0 : 3",
  "0010 100 0 : 3",
  "0010 100 0 : 3"
  );
   InputRecord FBm = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0110 000 0 : 2",
  "0010 000 0 : 2",
  "0010 001 0 : 2",
  "0010 001 0 : 2"
  );
   InputRecord FBh = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0110 000 0 : 2",
  "0010 000 0 : 2",
  "0010 010 0 : 2",
  "0010 010 0 : 2"
  );
  InputRecord rFBh = new InputRecord(this, 2, true, 
  "0100 000 0 : 2",
  "0101 000 0 : 2",
  "0001 000 0 : 2",
  "0001 010 0 : 2",
  "0001 010 0 : 2"
  );
  InputRecord rFBl = new InputRecord(this, 2, true, 
  "0100 000 0 : 3",
  "0101 000 0 : 3",
  "0001 000 0 : 3",
  "0001 100 0 : 3",
  "0001 100 0 : 3"
  );
  InputRecord Doublf = new InputRecord(this, 2, true,
  "0010 000 0 : 2",
  "000 000 0 : 1",
  "0010 000 0 : 2",
  "0010 000 0 : 2"
  );

  
  public void AIControll3(Fighter Opp){
    float aggro = dist(x, y, Opp.x, Opp.y)*0.1f + (Opp.maxHP/(Opp.curHP+1)) - (curHP/(maxHP+1));
    float distxy = dist(x, y, Opp.x, Opp.y), distx = dist(x, 0, Opp.x, 0), disty = dist(0, y, 0, Opp.y);
    
    if((distx > 420 || Opp.CurAction == Opp.Knockdown) && aggro >= 10 && curFartMeter < 70){
      Recorder = rFBl;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx <350 && disty > 60 && aggro >= 30 && curFartMeter > 35){
      Recorder = FBh;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx <370 && disty > 220 && aggro >= 40 && new InAir().cond(this, Opp) && curFartMeter >= 15){
      Recorder = rFBh;
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }else
    if(distx > 600 && aggro >= 60 && curFartMeter >= 30){
      Recorder = FBm;    
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }
    else
    if(distx > 660 && aggro >= 60){
      Recorder = Doublf;    
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }
    else
    if(distx > 400 && aggro >= 20 && new InAir().cond(this, Opp) && curFartMeter >= 15){
      Recorder = FBlightR;    
      if(Recorder.recAtEnd()){Recorder.reset(); Recorder = null;}
    }
    else Recorder = null;

  }
  
  
}

//Housefighter Endboss###############################################################################################################################################################################################
class F_HFBHaus extends Fighter{

  Action st_fHeavy, j_MidNormal2, st_punch, st_slide;
  
 F_HFBHaus(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "HFB/HFB-st/HFB-st");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_HFBHaus(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
 F_HFBHaus(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
 F_HFBHaus(int x, int y, PlControl Con){
    super(x,  y, Con);
  }
  
 F_HFBHaus(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
  
public void specSetup(){
  name = "HFBHouse";
   CollisionBox = new ColRect(0, 0, 150, 150);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "HFB/HFB-st/HFB-st");
      Standing = new Action("FH-st-idle", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
        
  int[] times1 = {6, 6, 6, 6, 6};
  Animation Anim1 = new Animation(times1, 0, 5, "HFB/HFB-fwalk/HFB-fwalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 3, 0);//3, 0);
  
  bWalk = new Action(new Animation(times1, 0, 5, "HFB/HFB-bwalk/HFB-bwalk"), false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2, 0);
  
  int[] times3 = {1, 2, 1, 4};
  Animation Anim3 = new Animation(times3, 0, 4, "FHouse/FH-n.jump/FH-n.jump");
  Anim3.loop = false;
  Jumping = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  Jumping.resetAnim = true;

      Jumping.updFrameDataArr(0, 5);
  Jumping.updFrameDataArr_float(0, 0, 0);
  Jumping.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  Jumping.addAllLists(1, 2, 0, -20);
  
  fDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  fDiaJump.resetAnim = true;

      fDiaJump.updFrameDataArr(0, 5);
      fDiaJump.updFrameDataArr_float(0, 0, 0);
      fDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
      fDiaJump.addAllLists(1, 2, 11, -20);
  
  bDiaJump = new Action("std_jump", Anim3, 0, 0, 0, 0, true, true, false, false);
  bDiaJump.resetAnim = true;

      bDiaJump.updFrameDataArr(0, 5);
  bDiaJump.updFrameDataArr_float(0, 0, 0);
  bDiaJump.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bDiaJump.addAllLists(1, 2, -7.5f, -20);
  
  Jumping2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false);  Jumping2.updFrameDataArr(0, 5); Jumping2.updFrameDataArr_float(0, 0, -10);
  fDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); fDiaJump2.updFrameDataArr(0, 5); fDiaJump2.updFrameDataArr_float(0, 10, -10);
  bDiaJump2 = new Action("std_djump", Anim3, 0, 0, 0, 0, false, false, false, false); bDiaJump2.updFrameDataArr(0, 5); bDiaJump2.updFrameDataArr_float(0, -10, -10);
  
  
  FDash = new Action(new Animation(new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4}, 40, 9, "HFB/HFB-run/HFB-run2p"), 0, 0, 0, 0, true, false, false, false);
  FDash.ActEffs = new ActEffect[]{new setActCurColTime(1, 0, new ActTimeCond2(1, 2), new facingCond(1) ), new SetForce(12, 0, 1, new ActTimeCond2(1, 1))};
    FDash.updFrameDataArr(0, 5);
    FDash.updFrameDataArr_float(0, 0.5f, 0);
    FDash.addAllLists(1, 2, 12, 0);
    FDash.addAllLists(2, 8, 2, 0);
    
  int[] ani5 = {1, 10};
  BDash = new Action(ani5, 0, 0, 0, 0, false, false, false, false);
  
  BDash.updFrameDataArr(0, 5);
  BDash.updFrameDataArr_float(0, -8, 0);
  BDash.addAllLists(1, 5, -10, 0);
  
    int[] times2 = {5};
  Crouching = new Action("fh-cr", new Animation(times2, 40, 1, "HFB/HFB-cr/HFB-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  Landing.AttAnim = new Animation(new int[]{4}, 40, 1, "HFB/HFB-landing/HFB-landing");
  
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

  
  /*int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD");
  //softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-hKD/FH-hKD");*/

  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);

  st_punch = new Action("HFB-punch", new Animation(new int[]{8, 8, 3, 4, 8, 4, 8}, 60, 10, 7,"HFB/HFB-punch/HFB-punch"), 
  new Condition[]{new fPButCond(4)}, 20, 12, 12, 3, 3, true, false, false, false);
  st_punch.ActEffs = new ActEffect[]{new CounterEff(0)};
    
  st_slide = new Action("HFB-slide", new Animation(new int[]{6, 6, 4, 3, 8,  3, 12, 4, 8, 4,  3, 8, 12}, 140, 13,"HFB/HFB-slide/HFB-slide"), 
  new Condition[]{new fPButCond(6)}, 20, 12, 12, Action.LOW, 4, true, true, true, false);
  
      
    Action[][] ActTab = {
  {st_punch, st_slide},
  {},
  {},
  { }
};

  fillActionsList(ActTab);
   
}
  public void st_Normals(){
        if(CurAction == fWalk){
      Force.x =  dirMult * sqrt(sq(sin(fWalk.AttAnim.timer* 0.1f))) * 6;
    }
  }
  
  public void cr_Normals(){
  }
  
  public void j_Normals(){
    }   
         
}

class F_NPC_0 extends Fighter{
   F_NPC_0(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
 F_NPC_0(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);  tint = true; AI_Controlled = true;
 }
  F_NPC_0(int x, int y, char[] charinputs, ControlDevice device){
   super(x, y, charinputs, device);
 }
 
 public void specSetup(){
   basicActSetup();
 }
 
 public void basicActSetup(){
    CollisionBox = new ColRect(0, 0, 150, 150);
      Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  int[] times0 = {5, 10, 10, 10};
  Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
        
  int[] times1 = {10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, 0, 7, "FHouse/FH-fWalk/FH-fWalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 3, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -70, 160, 180, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2, 0);
  
  int[] times3 = {1, 2, 1, 4};
  Animation Anim3 = new Animation(times3, 0, 4, "FHouse/FH-n.jump/FH-n.jump");
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
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-hKD/FH-hKD");

  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
}
 
}

class F_Enemy0 extends F_NPC_0{
  Action Special1; Action Special3;

 F_Enemy0(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
  }
  
 F_Enemy0(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);  tint = true; AI_Controlled = true;
    Special1 = ActionList.get(0).get(0); 
    Special3 = new ChangeOnEndAct(ActionList.get(0).get(1), new ChangeOnCondAct(ActionList.get(0).get(2), new Action (ActionList.get(0).get(3)) ));
 }
 
 F_Enemy0(int x, int y, char[] charinputs, ControlDevice device){
   super(x, y, charinputs, device);
 }
 
public void specSetup(){
  super.basicActSetup();
  maxHP = 50; curHP = maxHP;
  
  Condition[] Cond3 = {new fPButCond(5)};
    int[] times17 = {2, 2, 3, 2, 4, 6, 2, 1, 1, 2, 2, 2};
    Animation Anim17 = new Animation(times17, 50, 12,"FHouse/FH-st.Heavy/FH-st.Heavy");
  HeavyNormal = new Action("FH-st-heay", Anim17, Cond3, 16, 12, 20, Action.MID, 5, true, false, false, false);
  
  int[] times24 = {2, 2, 2, 2, 4, 2, 2, 2};
  int[][] DPvars = {{6, 2, 3}, {6, 3, 2, 3, 6}, {6, 3, 2, 3}, {6, 2, 6}};
  Condition[] Cond4 = {new comfPButC(DPvars), new fPButCond(5)};
  Special1 = new Action("FH-sp1", new Animation( times24, 50, 8, "FHouse/FH-sp1/FH-1special"), Cond4, 18, 16, 6, Action.MID, 20, true, true, false, false);
   
    int[] times21p3 = {4, 4, 4, 4, 4};
    Action Special3p3 = new Action( //"HH-cr-Light",
    new Animation(times21p3, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-3p"), 6, 5, 4, 0, true, true, false, false);
    
    Action FillAct = Special3p3;
    FillAct.updFrameDataArr(0, 20);
  FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[] times21p2 = {2, 2, 2};
    Action Special3p2 = new ChangeOnCondAct( "FH-sp3p2",
    new Animation(times21p2, 25, 50, 3,"FHouse/FH-sp3/FH-3sp-2p"), 20, 10, 20, Action.HIGH, true, false, true, false, Special3p3);
    
         FillAct = Special3p2;
    FillAct.updFrameDataArr(0, 60);
  FillAct.updFrameDataArr_float(0, 16, 18);
   //FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
    
    int[][] revFBvars = {{2, 1, 4}, {2, 1, 4, 7}};
    Condition[] Cond6 = {new InAir(), new comfPButC(revFBvars), new fPButCond(4)};
    int[] times21p1 = {2, 2, 2, 2, 2};
    Special3 = new ChangeOnEndAct( //"HH-cr-Light",
    new Animation(times21p1, 25, 50, 5,"FHouse/FH-sp3/FH-3sp-1p"), 6, 5, 4, Action.AIR, false, false, false, false, Special3p2); 
    Special3.Conds = Cond6; Special3.attWeight = 30;
          FillAct = Special3;
    FillAct.updFrameDataArr(0, 10);
    FillAct.updFrameDataArr_float(0, 0, 0);
   FillAct.HurtBoxCollect.get(0).add(new ColCircle(0, -175, 160, 330));
   
  Action[][] ActTab = { {Special1, Special3, Special3p2, Special3p3},
   {}, {}, {}
  };
  fillActionsList(ActTab);
   
}
 
  public void AI_Controll(Fighter Opp){
    if(new Grounded().cond(this, Opp) && CurAction == Standing){
      if( abs(Opp.x - x) <= 300){
        changeAction(Special1);
      }
      else{changeAction(fWalk);}
    }
    else if(new InAir().cond(this, Opp) && CurAction == Standing){
        changeAction(Special3);
    }
    
  }
  
  public void AIControll2(Fighter Opp){
  }

  public void st_Normals(){
  }
  
  public void cr_Normals(){
  }
  
  public void j_Normals(){          
  }
  
}

class F_Enemy1 extends F_NPC_0{
  
  F_Enemy1(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
      int[] times0 = {10, 10, 10, 10, 10, 10, 10, 10};
      Animation Anim0 = new Animation(times0, 0, 8, "HHouse/HH-st.idle/HH-st.idle");
      Standing = new Action("HH-st-idle", Anim0, false);
  }
  
   F_Enemy1(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);  tint = true; AI_Controlled = true;
    cr_MidNormal = ActionList.get(0).get(0); Standing = ActionList.get(0).get(1); 
 }
  F_Enemy1(int x, int y, char[] charinputs, ControlDevice device){
   super(x, y, charinputs, device);
 }
 
public void specSetup(){
  super.basicActSetup();
      int[] times2 = {5, 10, 10, 10};
  Standing = new Action(new Animation(times2, 0, 4, "FHouse/FH-cr/HF-cr"), false);
  Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
    int[] times19 = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
  Animation Anim19 = new Animation(times19, 50, 15, "FHouse/FH-cr.Mid/FH-cr.Mid");
  cr_MidNormal = new Action("FH-cr-med", Anim19, new Condition[]{}, 60, 10, 10, Action.LOW, 4, true, true, true, false);
  cr_MidNormal.setForceAtDur[0][0] = 5.0f;
  
  CollisionBox = new ColRect(0, 0, 150, 80);
    Action[][] ActTab = { {cr_MidNormal, Standing},
   {}, {}, {}
  };
  fillActionsList(ActTab);
}

  public void AI_Controll(Fighter Opp){
    if(new Grounded().cond(this, Opp) && CurAction == Standing){
      if( abs(Opp.x - x) <= 300){
        changeAction(cr_MidNormal);
      }
      //else{changeAction(fWalk);}
    }
    /*else if(new InAir().cond(this, Opp) && CurAction == Standing){
        changeAction(Special3);
    }*/
    
  }
  
  public void AIControll2(Fighter Opp){
  }

 
}

//KUGELENEMY###############################################################################################################################################################################################
class F_KEnem extends Fighter{
  Action Fireball;
  
 F_KEnem(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5, 10, 10, 10};
      Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_KEnem(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
      int[] times0 = {5, 10, 10, 10};
      Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_KEnem(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
 F_KEnem(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
  
public void specSetup(){
  name = "BallEnemy";
   CollisionBox = new ColRect(0, 0, 80, 80);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  
  int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "KEnem/KE-standing/KE-st");
      Standing = new Action("KE-st", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
        
  int[] times1 = {10, 10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, 0, 8, "KEnem/KE-fwalk/KE-fwalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 100, 100, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 4, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 100, 100, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2.5f, 0);
  
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action("fh-cr", new Animation(times2, 0, 4, "FHouse/FH-cr/HF-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "KEnem/hitStun/hitStun"); LHit = HHit; HBlock = HHit; LBlock = HHit;
  
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
  
  /*int[] times0_0 = {2,2}; 
  softKD.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-sKD/FH-air-sKD");
  //softKD.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 200, 160, 0, 0, -1));
  Knockdown.AttAnim = new Animation(times0_0, 0, 1,"FHouse/FH-hKD/FH-hKD");*/
  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);
  
    int[] times14 = {3, 2, 5, 2, 3, 3, 3, 3, 3, 3, 2, 3, 3, 3, 3};
  Condition[] Cond1 = {new fPButCond(6)};    
  Animation Anim14 = new Animation(times14, 0, 15, "KEnem/KE-Att2/KE-2Att");
  LightNormal = new Action("KE-Att2", Anim14, Cond1, 16, 5, 4, Action.MID, 1, true, true, false, false);
  LightNormal.ActEffs = new ActEffect[]{};
  
    int[] etimes1 = {3, 3, 5, 5, 3, 3, 3, 3};
       Projectile AcidProj = new Projectile(0.0f, 0.0f, 0, -80, 40, 40, 7.0f, 0.0f, 1, 60, 12, 6, 6, false, false, false, true, true) ;
   AcidProj.setAnims(new Animation(new int[]{10}, 0, 0, 1, "Projectiles/AcidShoot/AcidShoot"), new Animation(etimes1, 0, 0, 8, "Effekte/BettDestrEff/FH-BettEff"));
                  int[] times32 = {2, 6, 2, 6, 2,
                2, 2, 6, 2, 2,
              2};
   Fireball = new Action( new Animation( times32, 0, 11, "KEnem/KE-Att1/KE-1Att"), 32, 10, 4, Action.MID, true, false, false, false);
   Fireball.ActEffs = new ActEffect[]{ new ProjAddEff( AcidProj, 1, new ActTimeCond2(1, 1) ),  new ProjAddEff( AcidProj, -1, new ActTimeCond2(1, 1) )}; 
   Fireball.Conds = new Condition[]{new fPButCond(4)}; Fireball.attWeight = 30;
     Fireball.updFrameDataArr(0, 19);
  Fireball.updFrameDataArr_float(0, 0, 0);
  Fireball.HurtBoxCollect.get(0).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
  Fireball.addAllLists(1, 25, 0, 0);
  Fireball.HurtBoxCollect.get(1).add(new ColCircle( 0, -100, 180, 180, 0, 0, -1));
    
    Action[][] ActTab = {
  {LightNormal, Fireball},
  {},{},{}
};

  fillActionsList(ActTab);
}

public void stunStatesAnimSetup(){
      AnimKD = new Animation(new int[]{2, 4, 2, 20} , 0, 4, "KEnem/knockDown/KE-kd"); 
  AnimKD.loop = false;
  AnimKDreturn = new Animation(new int[]{4, 4, 4, 4} , 0, 4,"KEnem/KDreturn/KE-kd"); 
  AnimAirHit = new Animation(new int[]{7, 7, 6} , 0, 3,"KEnem/airHitstun/KE-kd");
  AnimAirHit.loop = false;
}

  public void standingStateReturn(Fighter Opp){
    if(y == GROUNDHEIGHT || CollisionBox.bottom) curAirActions = maxAirActions;
    
          if((y >= GROUNDHEIGHT || CollisionBox.bottom) && CurAction.attKind == 4) changeAction(Standing);
      else CurAction.playAction2(this, Opp, Standing); 
  }

  public void jump(){} public void dash(){}

  public void st_Normals(){
  }
  
  public void cr_Normals(){
  }
  
  public void j_Normals(){
    }   
         
}

//KUGELENEMY###############################################################################################################################################################################################
class F_DEnem extends Fighter{
  Action inAir, ceilingStick;
  
 F_DEnem(int x, int y, char[] charinputs){
    super(x,  y, charinputs);
      int[] times0 = {5, 10, 10, 10};
      Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_DEnem(int x, int y, char[] charinputs, boolean tint, boolean AI_Controlled){
    super(x, y, charinputs, tint, AI_Controlled);
          int[] times0 = {5, 10, 10, 10};
      Animation Anim0 = new Animation(times0, 0, 4, "FHouse/FH-st.idle/FH-st.idle");
      Standing = new Action("FH-st-idle", Anim0, false);
  }
  
 F_DEnem(int x, int y, char[] charinputs, ControlDevice device){
    super(x,  y, charinputs, device);
  }
  
 F_DEnem(int x, int y, char[] charinputs, Fighter F){ //copy constructer für speichersamkeit, beziehen Animations und gewisse unmutierbare Attackendaten
    super( x, y, charinputs, F);
 }
  
public void specSetup(){
  name = "KegelEnemy";
   CollisionBox = new ColRect(0, 0, 80, 250);
    Sprs = new PImage[18];
        for(int i = 0; i < Sprs.length; i++){
    String datName = "FHouse/FHaus_spr" + i + ".png";
    Sprs[i] = loadImage(datName);
  }
  
  
  int[] times0 = {5};
  Animation Anim0 = new Animation(times0, 0, 1, "DEnem/DE-standing/DE-st");
      Standing = new Action("KE-st", Anim0, false);
      //Standing.HurtBoxCollect.get(0).add(new ColCircle( 0, -105, 200, 160, 0, 0, -1));
      //Standing.updFrameDataArr(0, 1);
        
  int[] times1 = {10, 10, 10, 10, 10, 10, 10, 10};
  Animation Anim1 = new Animation(times1, 0, 8, "KEnem/KE-fwalk/KE-fwalk");
  fWalk = new Action(Anim1, false);//0, 0, 0, 0, true, false, false, false);
  fWalk.addingForce = false;
  fWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 100, 100, 0, 0, -1));
  fWalk.updFrameDataArr(0, 1);
  fWalk.updFrameDataArr_float(0, 4, 0);//3, 0);
  
  bWalk = new Action(Anim1, false);
  bWalk.addingForce = false;
  bWalk.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 100, 100, 0, 0, -1));
  bWalk.updFrameDataArr(0, 1);
  bWalk.updFrameDataArr_float(0, -2.5f, 0);
  
  
    int[] times2 = {5, 10, 10, 10};
  Crouching = new Action("fh-cr", new Animation(times2, 0, 4, "FHouse/FH-cr/HF-cr"), false);
  Crouching.HurtBoxCollect.get(0).add(new ColCircle( 0, -50, 160, 100, 0, 0, -1));
  
  HHit = new Animation(times1, 0, 1, "KEnem/hitStun/hitStun"); LHit = HHit; HBlock = HHit; LBlock = HHit;
  
  int[] ani9 = {6};
  Blocking = new Action(ani9);
  Blocking.updFrameDataArr(0, 2); 
  Blocking.updFrameDataArr_float(0, 0, 0);
  Blocking.HurtBoxCollect.get(0).add(new ColCircle( 0, -20, 200, 160));
  
    //int[] ani13 = {11};
  //BeingGrapped = new Action(ani13,  0, 0, 0, 0, false, true, false, false);

  inAir = new Action("KE-st", new Animation(times0, 0, 1, "DEnem/DE-inAir/DE-inAir"), false); inAir.addingForce = false;
  ceilingStick = new Action("KE-st", new Animation(times0, 0, 1, "DEnem/DE-stickCeiling/DE-stick"), false); ceilingStick.gravityActive = false; ceilingStick.addingForce = false;
  
  /*  int[] times14 = {3, 2, 5, 2, 3, 3, 3, 3, 3, 3, 2, 3, 3, 3, 3};
  Condition[] Cond1 = {new fPButCond(6)};    
  Animation Anim14 = new Animation(times14, 0, 15, "KEnem/KE-Att2/KE-2Att");
  LightNormal = new Action("KE-Att2", Anim14, Cond1, 16, 5, 4, Action.MID, 1, true, true, false, false);
  LightNormal.ActEffs = new ActEffect[]{};*/
  
  Action j_LightNormalP2 = new Action("DE-Att2p2", new Animation(new int[]{4, 4, 4, 4, 4, 2}, 0, 0, 6, "DEnem/DE-2Att/p2/DE-2Att"), 
  new Condition[]{}, 16, 5, 4, Action.MID, 1, true, false, false, false);
  j_LightNormalP2.ActEffs = new ActEffect[]{};
  
  j_LightNormal = new Action("DE-Att2", new Animation(new int[]{4, 4, 100}, 0, 60, 3, "DEnem/DE-2Att/DE-2Att"), 
  new Condition[]{new InAir(), new fPButCond(5)}, 16, 5, 4, Action.HIGH, 1, true, false, false, false);
  j_LightNormal.ActEffs = new ActEffect[]{new ChangeActTo(j_LightNormalP2, 1, new Grounded())}; j_LightNormal.collision = false;
  
  j_MidNormal = new Action("DE-Att4", new Animation(new int[]{8, 4, 6, 4, 4, 4, 4}, 0, 350, 7, "DEnem/DE-Att4/DE-4Att"), 
  new Condition[]{new fPButCond(6)}, 16, 5, 4, Action.HIGH, 1, false, false, false, false);
  j_MidNormal.ActEffs = new ActEffect[]{}; j_LightNormal.collision = false;
  
  MidNormal2 = new Action("DE-Att1", new Animation(new int[]{6, 4, 2, 4, 2, 4, 2, 4, 4, 2}, 0, 10, "DEnem/DE-1Att/DE-1Att"), 
  new Condition[]{new fPButCond(5)}, 16, 5, 4, Action.MID, 1, true, true, false, false);
  MidNormal2.ActEffs = new ActEffect[]{};
  
  HeavyNormal = new Action("DE-Att3", new Animation(new int[]{4, 4, 4, 4, 6, 6, 4, 4, 4, 4}, 50, 10, "DEnem/DE-Att3/DE-3Att"), 
  new Condition[]{new fPButCond(4)}, 16, 5, 4, Action.MID, 1, true, false, false, false);
  HeavyNormal.ActEffs = new ActEffect[]{};
    
    Action[][] ActTab = {
  {HeavyNormal, MidNormal2, j_MidNormal},
  {},{j_LightNormal},{}, 
  {j_LightNormal}, //InAirState
  {j_LightNormal, j_MidNormal}  //ceilingState
};

  fillActionsList(ActTab);
}

public void fighterActionsExtra(){
    if(ceilStick){ checkSingleActList(this, ActionList.get(5)); if(inputs[0] && inputs[4]) ceilStick = false;}
    else if(y < GROUNDHEIGHT && !CollisionBox.bottom) checkSingleActList(this, ActionList.get(4));
           
}

public void stunStatesAnimSetup(){
      AnimKD = new Animation(new int[]{2, 4, 2, 20} , 0, 4, "KEnem/knockDown/KE-kd"); 
  AnimKD.loop = false;
  AnimKDreturn = new Animation(new int[]{2, 2, 2, 2} , 0, 4,"KEnem/KDreturn/KE-kd"); 
  AnimAirHit = new Animation(new int[]{7, 7, 6} , 0, 3,"KEnem/airHitstun/KE-kd");
  AnimAirHit.loop = false;
}

boolean ceilStick = false;
  public void standingStateReturn(Fighter Opp){
    if(CollisionBox.top){ changeAction(ceilingStick); ceilStick = true;}
    if(y == GROUNDHEIGHT || CollisionBox.bottom || CurAction == j_LightNormal) ceilStick = false;
    if(y == GROUNDHEIGHT || CollisionBox.bottom) curAirActions = maxAirActions;
    
          if((y >= GROUNDHEIGHT || CollisionBox.bottom) && CurAction.attKind == 4) changeAction(Standing);
          else if(ceilStick) CurAction.playAction2(this, Opp, ceilingStick);
          else if(y < GROUNDHEIGHT && !CollisionBox.bottom) CurAction.playAction2(this, Opp, inAir);
      else CurAction.playAction2(this, Opp, Standing); 
  }

  public void jump(){} public void dash(){}

  public void st_Normals(){
  }
  
  public void cr_Normals(){
  }
  
  public void j_Normals(){
    }   
         
}

class F_Edit extends Fighter{
  String F_datnam = "none";
  public void specSetup(){}
} 
class Action{
  final static int NOTHING = 0, HIGH = 1, LOW = 2, MID = 3, AIR = 4, GRAB = 5;
  final static int NORMAL = 0, SPECIAL = 1, SUPER = 2,  HITSTATE = 100;
  final static int WALLSTICK = 0, WALLBOUNCE = 1, JUGGLE = 2, GROUNDBOUNCE = 3, STAGGER = 4; //Opp.WallStick, Opp.WallBounce, Opp.Juggle, Opp.GroundBounce, Opp.Stagger
  int attKind = NOTHING;
  int attWeight = 0, attRank = 0; //100 = hitstate, 0 = Normal, 1 = Special, 2 = Super 
  
  int curMoveDur = 0, curCollumn = 0;
  int affHitStunT = 10, affBlockStunT = 5;
  int damage = 0, superNeed = 0;
  float gravMult = 1.f, fricMult = 1.f, pushBackMult = 1;
  
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
  
  public void addEffs(ActEffect... Effs){
    ActEffect temp[] = new ActEffect[ActEffs.length+Effs.length];
    for(int i = 0; i < ActEffs.length; i++){
      temp[i] = ActEffs[i];
    }
    for(int i = 0; i < Effs.length; i++){
      temp[ActEffs.length+i] = Effs[i];
    }
    ActEffs = temp;
  }
  
  public void playAction2(Fighter Pl, Fighter Opp, Action ToSetTo){
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
  
  public void clearAndCurBoxes(Fighter Pl){ // means adding current boxes
                Pl.HitBoxes.clear();
                Pl.HurtBoxes.clear();
       for(int i = 0; i < HitBoxCollect.get(curCollumn).size(); i++){
           Pl.HitBoxes.add(HitBoxCollect.get(curCollumn).get(i));
         }
         
       for(int i = 0; i < HurtBoxCollect.get(curCollumn).size(); i++){
           Pl.HurtBoxes.add(HurtBoxCollect.get(curCollumn).get(i));
         }
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
      for(ActEffect a : ActEffs) a.reset();
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

class PH_sp1 extends Action{  
      F_PHaus F = null;
  
  PH_sp1(String datnam, Animation AttAnim, Condition[] Conds, int affHitStunT, int affBlockStunT, int damage, int attKind, int attWeight,
      boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, F_PHaus F){
        super( datnam, AttAnim,Conds, affHitStunT, affBlockStunT, damage, attKind, attWeight, gravityActive, addingForce, knocksDown, multiHit);
        this.F = F;
      }
      
      public void specialEffect(Fighter Pl, Fighter Opp){
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
      
  public void specialEffectOnEnd(Fighter Pl, Fighter Opp){
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
  
    public void specialEffectOnHit(Fighter Pl, Fighter Opp){
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
          public void specialEffectOnEnd(Fighter Pl, Fighter Opp){
        Pl.CurAction.reset();
        Pl.changeAction(ActToChangeTo);
        AttAnim.Reset();
  }  
    public void specialEffectOnHit(Fighter Pl, Fighter Opp){
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
  
    public void setAct(Action a){
      ActToChangeTo = a;
    }
    public Action getAct(){
      return ActToChangeTo;
    }
  
    public void specialEffectOnEnd(Fighter Pl, Fighter Opp){
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
  
     public void alwaysSpecialEffect(Fighter Pl, Fighter Opp){
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
  
     public void specialEffectOnEnd(Fighter Pl, Fighter Opp){
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
        this.grabOppPos = grabOppPos; hitCancel = false;
  }
            GrabAction( Animation sprsIds, int affHitStunT, int affBlockStunT, int damage, int attKind, boolean gravityActive, boolean addingForce, boolean knocksDown, boolean multiHit, float[][] grabOppPos){
        super( sprsIds, affHitStunT, affBlockStunT, damage, attKind, gravityActive, addingForce, knocksDown, multiHit);
        this.grabOppPos = grabOppPos; hitCancel = false;
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
  
  public void specialEffect(Fighter Pl, Fighter Opp){
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
  
      public void specialEffect(Fighter Pl, Fighter Opp){
    if(curCollumn == 1 && (Pl.x <= Camerabox.x - Camerabox.br/2 + 100 || Pl.x >= Camerabox.x + Camerabox.br/2 - 100 || (Pl.CollisionBox.rside || Pl.CollisionBox.lside)) ){
      for(int i = 1; i < 4; i++){
      PH_Apple p = new PH_Apple(Pl.x + 300*Pl.dirMult*i, -400, 80, 120,
      Pl.dirMult * fx , 0.2f, m, exTimer, affHitStunT, affBlockStunT, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit, F);
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
  
  public void draw(){
    rect(x + addx, y + addy - ho, br, ho);
  }
  
  public void setColBools(boolean toSet){
    bottom = toSet; top = toSet; lside = toSet; rside = toSet;
  }
  
  public void colCheckRect2( Fighter Pl, ColRect other){ //für Fighter - ColRect Kollisionen
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
  
    public void colCheckRect( Fighter Pl, Fighter Opp){ // für Fighter - Fighter Kollisionen
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
  public void draw(Fighter Pl, Fighter Opp){ Effect(Pl, Opp);} 
  public void Effect(Fighter Pl, Fighter Opp){
  }
  
  public boolean cond(Fighter Pl, Fighter Opp){
    if(Conds.length <= 0){ return true;}
    for(int i = 0; i < Conds.length; i++){
      if(!Conds[i].cond(Pl, Opp)){
        return false;
      }
    }
    return true;
  }
  
  public void reset(){}
  
}

class GatlingEff extends ActEffect{
  Action[] ActList;
  
  GatlingEff(Action... ActList){
    this(new Condition[0], ActList);
  }
  GatlingEff(Condition[] Conds, Action... ActList){
    this.ActList = ActList; this.Conds = Conds; this.whereUsed = 1;
  }
  
  public void Effect(Fighter Pl, Fighter Opp){
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
  
 public void Effect(Fighter Pl, Fighter Opp){
    if(Pl.cancelWindow <= 0) return; Pl.changeAction(ToChange);
  }
  
}

class AddHP extends ActEffect{
  int addHP = 0;
  AddHP(int addHP, int whereUsed, Condition... Conds){
    this.addHP = addHP; this.whereUsed = whereUsed; this.Conds = Conds;
  }
  public void Effect(Fighter Pl, Fighter Opp){Pl.curHP -= addHP;}
}

class OppEff extends ActEffect{ ActEffect Eff;
  OppEff(ActEffect Eff, int whereUsed, Condition... Conds){
    this.Eff = Eff; this.whereUsed = whereUsed; this.Conds = Conds;
  }
  public void Effect(Fighter Pl, Fighter Opp){Eff.Effect(Opp, Pl);}
}

class setActCurColTime extends ActEffect{ int setCurCollumn = 0, setCurTime = 0;
  setActCurColTime(int setCurCollumn, int setCurTime, Condition... Conds){ this.setCurCollumn = setCurCollumn; this.setCurTime = setCurTime; this.Conds = Conds; this.whereUsed = 1; }
  public void Effect(Fighter Pl, Fighter Opp){ if(setCurCollumn < Pl.CurAction.HitBoxCollect.size()){Pl.CurAction.curCollumn = setCurCollumn; Pl.CurAction.curMoveDur = setCurTime; };  }
}

class SetForce extends ActEffect{ float fx = 0, fy = 0;
  SetForce(float fx, float fy, int whereUsed, Condition... Conds){ this.fx = fx; this.fy = fy; this.whereUsed = whereUsed; this.Conds = Conds; }
  public void Effect(Fighter Pl, Fighter Opp){ Pl.Force.x = fx*Pl.dirMult; Pl.Force.y = fy; }  
}

class SelfCancEff extends ActEffect{ Action Act = null;
  SelfCancEff(Action Act){ this.Act = Act; this.whereUsed = 1;}
  SelfCancEff(Action Act, int whereUsed, Condition[] Conds){ this.Act = Act; this.whereUsed = whereUsed; this.Conds = Conds;}
  public void Effect(Fighter Pl, Fighter Opp){ 
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
  
  public void Effect(Fighter Pl, Fighter Opp){ 
    
    if(FirstTime){
      for(ActEffect a : Effs){
        if(a.cond(Pl, Opp)) a.Effect(Pl, Opp);
      }
      FirstTime = false;
    }
 
  }
  public void reset(){FirstTime = true;}
  
}

class TimerEff extends ActEffect{
  int curTime = 0; int maxTime = 2; ActEffect[] Effs;
  TimerEff(int maxTime, Condition[] Conds, ActEffect... Effs){
    this.maxTime = maxTime; this.Effs = Effs; this.Conds = Conds; this.whereUsed = 1;
  }
  
  TimerEff(int maxTime, ActEffect... Effs){
    this(maxTime, new Condition[]{new Condition()}, Effs);
  }
  
  public void Effect(Fighter Pl, Fighter Opp){ 
    if(curTime < maxTime) curTime++;
    
    if(curTime >= maxTime){
      for(ActEffect a : Effs){
        if(a.cond(Pl, Opp)) a.Effect(Pl, Opp);
      }
      curTime = 0;
    }
 
  }
  
  public void reset(){ curTime = 0;}
  
}

class PutInHitSt extends ActEffect{
  int hitStun = 0, addDamage = 0; 
  PutInHitSt(int hitStun, int addDamage, Condition... Conds){
    this.hitStun = hitStun; this.addDamage = addDamage; this.Conds = Conds; whereUsed = 1;
  }
  public void Effect(Fighter Pl, Fighter Opp){
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
  
  public void Effect(Fighter Pl, Fighter Opp){
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
    public void Effect(Fighter Pl, Fighter Opp){
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
    public void Effect(Fighter Pl, Fighter Opp){
      Animation a = Pl.CurAction.AttAnim; a.X_coords = ax; a.Y_coords = ay;
  }
}

class ProjAddEff extends ActEffect{
  Projectile P; int dirMult = 1; //Animation Anim, destrEff;
  ProjAddEff(Projectile P, int dirMult, Condition... Conds){ this.P = P; this.dirMult = dirMult; this.whereUsed = 1; this.Conds = Conds;}
  
  public void Effect(Fighter Pl, Fighter Opp){ 
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
  public void Effect(Fighter Pl, Fighter Opp){ VisEffectsList.add( new VisualEffect( Pl.x+addX*Pl.dirMult, Pl.y+addY, VisEffAnim, 1, Pl.dirMult) ); }
}

class WH_stance extends ActEffect{  F_WHaus F = null;
  WH_stance( F_WHaus F, int whereUsed, Condition... Conds){super(whereUsed, Conds); this.F = F;}
    public void Effect(Fighter Pl, Fighter Opp){
      F.stance = true;
    }
}

class FirstHitEff extends GravEff{
  FirstHitEff(int actCollumn, int actTime, boolean setGrav){
    super(actCollumn, actTime, setGrav);
  }
  public void Effect(Fighter Pl, Fighter Opp){ Action PAct = Pl.CurAction;
    if(PAct.curCollumn == actCollumn && PAct.curMoveDur == actTime){ PAct.firstHit = setGrav;}
  }
}

class GravEff extends ActEffect{
  int actCollumn = 0, actTime = 0; boolean setGrav = true;
  GravEff(int actCollumn, int actTime, boolean setGrav){
    this.actCollumn = actCollumn; this.actTime = actTime; this.setGrav = setGrav;
    this.whereUsed = 1;
  }
  
  public void Effect(Fighter Pl, Fighter Opp){ Action PAct = Pl.CurAction;
    if(PAct.curCollumn == actCollumn && PAct.curMoveDur == actTime){
      PAct.gravityActive = setGrav;
    }
  }
  
}

class KDownEff extends GravEff{
  KDownEff(int actCollumn, int actTime, boolean setForceAdd){
    super(actCollumn, actTime, setForceAdd);
  }
  
  public void Effect(Fighter Pl, Fighter Opp){ Action PAct = Pl.CurAction;
    if(PAct.curCollumn == actCollumn && PAct.curMoveDur == actTime){
      PAct.knocksDown = setGrav;
    }
  }
  
}

class ForceAddEff extends GravEff{
  ForceAddEff(int actCollumn, int actTime, boolean setForceAdd){
    super(actCollumn, actTime, setForceAdd);
  }
  
  public void Effect(Fighter Pl, Fighter Opp){ Action PAct = Pl.CurAction;
    if(PAct.curCollumn == actCollumn && PAct.curMoveDur == actTime){
      PAct.addingForce = setGrav;
    }
  }
  
}

class ResAnimEff extends ActEffect{
  
  ResAnimEff(int whereUsed, Condition... Conds){
    this.whereUsed = whereUsed; this.Conds = Conds;
  }
  public void Effect(Fighter Pl, Fighter Opp){
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
 public void Effect(Fighter Pl, Fighter Opp){
   if(Pl.CurAction.curCollumn == actCollumn && Pl.CurAction.curMoveDur == actTime){
     Sound.cue(0); Sound.play();
   }
  }
  
}

class CounterEff extends ActEffect{
  CounterEff(int whereUsed){this.whereUsed = whereUsed;}
    CounterEff(int whereUsed, Condition... Conds){ super(whereUsed, Conds);}

  public void Effect(Fighter Pl, Fighter Opp){
  }
}

class WallBounceEff extends ActEffect{
    WallBounceEff(int whereUsed, Condition[] Conds){
       super(whereUsed, Conds);
  }
  public void Effect(Fighter Pl, Fighter Opp){
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
  float mult = 1.f;
  
  AddOwnForcToOpp(int whereUsed, float mult, Condition... Conds){
    super(whereUsed, Conds); this.mult = mult;
  }
  
   public void Effect(Fighter Pl, Fighter Opp){
     Opp.Force.x = Pl.Force.x * mult;
     Opp.Force.y = Pl.Force.y * mult;
   }
  
}

class ChangeActTo extends ActEffect{Action ActToSetTo;
  ChangeActTo(Action ActToSetTo, int whereUsed, Condition... Conds){
    super(whereUsed, Conds); this.ActToSetTo = ActToSetTo;
  }
     public void Effect(Fighter Pl, Fighter Opp){
       Pl.changeAction(ActToSetTo);
     }
  
}

class OBS_setSFToM extends ActEffect{ //set Stands Force to Master's Force
  F_OBHaus F;
  OBS_setSFToM(F_OBHaus F, int whereUsed, Condition... Conds){ this.F = F; this.whereUsed = whereUsed; this.Conds = Conds;}
  public void Effect(Fighter Pl, Fighter Opp){F.Stand.Force.x = F.Force.x; F.Stand.Force.y = F.Force.y;}
}

class OBS_changeXY extends ActEffect{ F_OBHaus F; int x = 0, y = 0;
  OBS_changeXY(F_OBHaus F, int x, int y, Condition... Conds){this.F = F; this.x = x; this.y = y; this.whereUsed = 1; this.Conds = Conds;}
  public void Effect(Fighter Pl, Fighter Opp){F.Stand.x = F.x + x*F.Stand.dirMult; F.Stand.y = F.y + y;} 
}

class changeToOwnXY extends ActEffect{int x = 0, y = 0;
  changeToOwnXY(int x, int y, Condition... Conds){this.x = x; this.y = y; this.whereUsed = 1; this.Conds = Conds;}
  public void Effect(Fighter Pl, Fighter Opp){Pl.x += this.x*Pl.dirMult; Pl.y += this.y;} 
}

class OBS_setState extends ActEffect{
  F_OBHaus F; int setTo = 0;
  OBS_setState(F_OBHaus F, int setTo, int whereUsed){ this.F = F; this.setTo = setTo; this.whereUsed = whereUsed;}
  OBS_setState(F_OBHaus F, int setTo, int whereUsed, Condition... Conds){ this.F = F; this.setTo = setTo; this.whereUsed = whereUsed; this.Conds = Conds;}
  public void Effect(Fighter Pl, Fighter Opp){F.Stand.State = setTo;} 
}

class OBS_setAction extends ActEffect{
  F_OBHaus F; Action setTo = null;
  OBS_setAction(F_OBHaus F, Action setTo, int whereUsed){ this.F = F; this.setTo = setTo; this.whereUsed = whereUsed;}
  OBS_setAction(F_OBHaus F, Action setTo, int whereUsed, Condition... Conds){ this.F = F; this.setTo = setTo; this.whereUsed = whereUsed; this.Conds = Conds;}
  public void Effect(Fighter Pl, Fighter Opp){F.Stand.changeAction(setTo);} 
}

class OBS_setStateAndAct extends ActEffect{
  F_OBHaus F; Action setTo = null; int state = 0;
  OBS_setStateAndAct(F_OBHaus F, Action setTo, int state, int whereUsed){ this.F = F; this.setTo = setTo; this.state = state; this.whereUsed = whereUsed;}
  OBS_setStateAndAct(F_OBHaus F, Action setTo, int state, int whereUsed, Condition... Conds){ this.F = F; this.setTo = setTo; this.state = state; this.whereUsed = whereUsed; this.Conds = Conds;}
  public void Effect(Fighter Pl, Fighter Opp){F.Stand.changeAction(setTo); F.Stand.State = state;} 
}

class PH_dirEff extends ActEffect{
  float fx = 0, fy = 0, rot = 0; int dir = 1;
  PH_dirEff(int whereUsed, Condition[] Conds, float fx, float fy, float rot, int dir ){
    super(whereUsed, Conds); this.fx = fx; this.fy = fy; this.rot = rot; this.dir = dir;
  }
  
    public void Effect(Fighter Pl, Fighter Opp){
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
    public void Effect(Fighter Pl, Fighter Opp){
      if(F.gearLevel+gearAdd >= 0 && F.gearLevel+gearAdd <= 3)
      F.gearLevel += gearAdd;
    }    
}

class Condition extends ActionModules{
  Condition(){}
  
  public boolean cond(Fighter Pl, Fighter Opp){
    return true;
  }
  public void EffectIfCond(Fighter Pl, Fighter Opp){
  }
}

class CamWallTouch extends Condition{
  
  public boolean cond(Fighter Pl, Fighter Opp){
    if((Pl.x - Pl.CollisionBox.br/2 - 10 <= Camerabox.x - Camerabox.br/2) || (Pl.x + Pl.CollisionBox.br/2 + 10 >= Camerabox.x + Camerabox.br/2) ){
      return true;
    }
    return false;
  }
  
}
    
class xDistOpp extends Condition{
  int dist = 0; boolean lt = true; xDistOpp(int dist, boolean lt){this.dist = dist; this.lt = lt;} 
  public boolean cond(Fighter Pl, Fighter Opp){ return (lt && abs(Pl.x - Opp.x) <= dist) || (!lt && abs(Pl.x - Opp.x) >= dist);}
}

class FalseCond extends Condition{
  Condition[] fConds;
  public FalseCond(Condition... fConds){this.fConds = fConds;}
  public boolean cond(Fighter Pl, Fighter Opp){ 
    for(Condition c : fConds){
      if(c.cond(Pl, Opp)) return false;
  }
return true;
}
  
}


class OBSH_checkState extends Condition{
  private F_OBHaus F; int state = 0; boolean b = true; 
  OBSH_checkState(F_OBHaus F, int state, boolean b){this.F = F; this.state = state; this.b = b;} 
   public boolean cond(Fighter Pl, Fighter Opp){return ((F.Stand.State == state) && b) || ((F.Stand.State != state) && !b);}
}

class OBSH_checkAction extends Condition{
  private F_OBHaus F; Action Act[]; boolean b = true; 
  OBSH_checkAction(F_OBHaus F, Action Act, boolean b){this.F = F; this.Act = new Action[]{Act}; this.b = b;} 
  OBSH_checkAction(F_OBHaus F, boolean b, Action... Act){this.F = F; this.Act = Act; this.b = b;}
   public boolean cond(Fighter Pl, Fighter Opp){
     for(Action a : this.Act){
       if( ((F.Stand.CurAction == a) && b) || ((F.Stand.CurAction != a) && !b) ) return true;
     }
     return false;
 }
}

class WH_checkStance extends Condition{
  private F_WHaus F; boolean b = true; WH_checkStance(F_WHaus F, boolean b){this.F = F; this.b = b;} public boolean cond(Fighter Pl, Fighter Opp){return F.stance == b;}
}

class WH_gearCheck extends Condition{
  private F_WHaus F; private int gearL_comp = 0;
  public WH_gearCheck(int gearL, F_WHaus F){ this.gearL_comp = gearL; this.F = F;}
    public boolean cond(Fighter Pl, Fighter Opp){ return gearL_comp <= F.gearLevel;}
}

class AnimCheck extends Condition{ //doesnt do anything
  private Animation compAnim;AnimCheck(Animation compAnim){this.compAnim = compAnim;}
  public boolean cond(Fighter Pl, Fighter Opp){ 
  return compAnim == Pl.CurAction.AttAnim || compAnim == Pl.CurAnimation;// || (compAnim != Pl.CurAction.AttAnim && !b); 
}
}

class OppCheck extends Condition{
  Condition[] Conds;
  OppCheck(Condition... Conds){ this.Conds = Conds; }
  
  public boolean cond(Fighter Pl, Fighter Opp){
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
  
  public boolean cond(Fighter Pl, Fighter Opp){
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
  public boolean cond(Fighter Pl, Fighter Opp){
    return Pl.CurAction == State;
  }
  
}

class CheckAttPart extends Condition{
  int part = 0; CheckAttPart(int part){this.part = part;}
  public boolean cond(Fighter Pl, Fighter Opp){ Action pact = Pl.CurAction; ArrayList<ColCircle> hl = pact.HitBoxCollect.get(pact.curCollumn);
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
  public boolean cond(Fighter Pl, Fighter Opp){Action PAct = Pl.CurAction;
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

  public boolean cond(Fighter Pl, Fighter Opp){Action PAct = Pl.CurAction;
    return PAct.curCollumn == actCurCollumn && PAct.curMoveDur == actCurTime;
  }
  
}

class ActTimeCond3 extends Condition{
  int actCurCollumn = 0, actCurCollumn2 = 0; //checks for between time 1 and time 2
  ActTimeCond3(int actCurCollumn, int actCurCollumn2){
    this.actCurCollumn = actCurCollumn; this.actCurCollumn2 = actCurCollumn2;
  }

  public boolean cond(Fighter Pl, Fighter Opp){Action PAct = Pl.CurAction;
    return (PAct.curCollumn >= actCurCollumn && PAct.curCollumn <= actCurCollumn2);
  }
  
}

class UseEffCond extends Condition{ //reassigns Eff Array to passed Action; consider renaming
  Condition[] Conds; ActEffect[] Effs; Action toAssignTo;
  UseEffCond(Action toAssignTo, Condition[] Conds, ActEffect[] Effs){
    this.toAssignTo = toAssignTo; this.Conds = Conds; this.Effs = Effs;
  }
  public boolean cond(Fighter Pl, Fighter Opp){
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
  public boolean cond(Fighter Pl, Fighter Opp){
    return Opp.counterState;
  }
}

class SuperCond extends Condition{
  int superNeed = 0;
  SuperCond(int superNeed){this.superNeed = superNeed;}
  
  public boolean cond(Fighter Pl, Fighter Opp){
    return this.superNeed <= Pl.curSuper;
  }
}

class Grounded extends Condition{
    public boolean cond(Fighter Pl, Fighter Opp){
    return Pl.CollisionBox.bottom || Pl.y >= GROUNDHEIGHT;
  }
}

class InAir extends Condition{
    public boolean cond(Fighter Pl, Fighter Opp){
    return Pl.y < GROUNDHEIGHT && !Pl.CollisionBox.bottom;
  }
}

class dirCombCond extends Condition{
  int[] motion = {5};
  dirCombCond(int[] motion){this.motion = motion;}
  public int[] getMotion(){ return motion; }
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

class ChargeCheck extends ButCond{
  int chargeAmount = 0;
  ChargeCheck(int ButIndex, int chargeAmount){ super(ButIndex); this.chargeAmount = chargeAmount;}
  public boolean cond(Fighter Pl, Fighter Opp){
    return Pl.inputChargeT[ButIndex] >= chargeAmount;
  }
}

class ChargeDirCheck extends Condition{
  int chargeAmount = 0, dir = 1;
  ChargeDirCheck(int chargeAmount, int dir){ this.chargeAmount = chargeAmount; this.dir = dir;}
  public boolean cond(Fighter Pl, Fighter Opp){
    return (Pl.inputChargeT[2] >= chargeAmount && Pl.dirMult*dir == -1) || (Pl.inputChargeT[3] >= chargeAmount && Pl.dirMult*dir == 1);//Pl.ChargeDirCheck(chargeAmount, dir);
  }
}

class VertForceCheck extends Condition{
  float speedNeed = 0; VertForceCheck(float speedNeed){this.speedNeed = speedNeed;}
  public boolean cond(Fighter Pl, Fighter Opp){
    return (speedNeed >= 0 && ( (Pl.Force.x >= speedNeed && Pl.dirMult == 1) || (Pl.Force.x*-1 >= speedNeed && Pl.dirMult == -1) ) )
    || (speedNeed <= 0 && ( (Pl.Force.x >= speedNeed*-1 && Pl.dirMult == -1) || (Pl.Force.x*-1 >= speedNeed*-1 && Pl.dirMult == 1) ) );
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

class facingCond extends Condition{
  int mult = 1;// forward facing
  facingCond(int mult){
    this.mult = mult;
  }
  
  public boolean cond(Fighter Pl, Fighter Opp){
    return ( (Pl.inputs[2] && Pl.dirMult*mult == 1) || (Pl.inputs[3] && Pl.dirMult*mult == -1) );
  }
}

class comfPButC extends Condition{
  int[][] motions = {{5},{5}};
  comfPButC(int[][] motions){ this.motions = motions;}
  public int[] getMotion(){ return motions[0]; }
  public boolean cond(Fighter Pl, Fighter Opp){
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
  public boolean cond(Fighter Pl, Fighter Opp){
    return Pl.CurAction.firstHit;
  }
}

class PH_FartCond extends Condition{
  int fartNeed = 0, fartConsume = 0; F_PHaus F;
  PH_FartCond(int fartNeed, int fartConsume, F_PHaus F){ this.fartNeed = fartNeed; this.fartConsume = fartConsume; this.F = F;}
  
  public boolean cond(Fighter Pl, Fighter Opp){
    if(fartNeed <= F.curFartMeter){
      F.curFartMeter -= fartConsume;
      return true;
    }
    return false;
  }
  
}
class Gamestate{
  boolean deletMe = false;
  public void setup(){}
  public void gameLogic(){}
  public void drawVisuals(){}
  public void keyPressed(){}
  public void keyReleased(){}
  public void finishState(){};
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
  
  public void setup(){}
  
  public void gameLogic(){
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
  
  public void drawVisuals(){}
  public void keyPressed(){}
  public void keyReleased(){}
}

class StageSelect extends Gamestate{
  int CurStageID = 0; Gamestate NextGS;
  MenuBox LeftBox = new MenuBox(initWidth/4-25, initHeight/2, 50, 200, "<", 180), RightBox = new MenuBox(initWidth-initWidth/4-25, initHeight/2, 50, 200, ">", 180),
  ChUrNeighBox = new MenuBox(50, 40, 800, 80, "CHOOSE YOUR NEIGHBOURHOOD!!", 255);
  Stage[] StageList = {new TrainingStage(), new BergGrossStage()};
  StageSelect(Gamestate NextGS){ this.NextGS = NextGS; }
  public void gameLogic(){
    if(LeftBox.clicked && CurStageID > 0)CurStageID--; if(RightBox.clicked && CurStageID < StageList.length-1) CurStageID++;
    StageBackground = StageList[CurStageID];
    if(ChUrNeighBox.clicked) CurGameState = NextGS;
  }
  public void drawVisuals(){
        camera(0+initWidth/2.0f, initHeight/2.0f, (initHeight/2.0f) / tan(PI*30.0f / 180.0f),
    0+initWidth/2.0f, initHeight/2.0f, 0,
    0, 1, 0);
    StageList[CurStageID].drawBackground();
    StageList[CurStageID].drawForeground();
    LeftBox.draw(); RightBox.draw(); ChUrNeighBox.draw();
  }
  public void keyPressed(){}
  public void keyReleased(){}
  
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
  
  public void setup(){}
  
  public void menu(Fighter P1, Fighter P2){
    Menu.draw(P1, P2);
    if(Menu.Backbox.clicked) menuOn = false;
  }
  
  public void gameLogic(){
    if(menuOn && !Rtest.Edit){ Menu.inGameEffect(Player1, Player2); return;}  

    if(Rtest.Edit)Rtest.LevelEditor();
    if(Rtest.Edit)return;
    
    for(Fighter P : Rtest.Players){
      P.CollisionBox.x = P.x - P.CollisionBox.br/2;  P.CollisionBox.y = P.y; 
        //Rtest.colCheckEnv(P);
        Rtest.colCheckEnv2(P,PApplet.parseInt(P.x-initWidth)/60+Rtest.roomWUnits/2, PApplet.parseInt(P.x+initWidth)/60+Rtest.roomWUnits/2, 
        PApplet.parseInt(P.y-initHeight)/60+Rtest.roomHUnits/2, PApplet.parseInt(P.y+initHeight/2)/60+Rtest.roomHUnits/2);
        P.gameLogic(P);
        P.manualFacing();
   P.CollisionBox.x = P.x - P.CollisionBox.br/2;  P.CollisionBox.y = P.y; 
   
   //float[] posf = Rtest.colCheckEnv2(P, 0, 0, Rtest.roomWUnits, Rtest.roomHUnits);
   //Rtest.colCheckEnv2(P,int(Player1.x-initWidth)/60+Rtest.roomWUnits/2, int(Player1.x+initWidth)/60+Rtest.roomWUnits/2, 
    //int(Player1.y-initHeight)/60+Rtest.roomHUnits/2, int(Player1.y+initHeight/2)/60+Rtest.roomHUnits/2);
   /*float[] posf2 = Rtest.colCheckEnv(P), posf3 = new float[2];
   if(posf[0] <= posf2[0]) posf3[0] = posf[0]; else posf3[0] = posf2[0];
   if(posf[1] <= posf2[1]) posf3[1] = posf[1]; else posf3[1] = posf2[1];*/
   P.updatePos(Rtest.colCheckEnv2(P,PApplet.parseInt(P.x-initWidth)/60+Rtest.roomWUnits/2, PApplet.parseInt(P.x+initWidth)/60+Rtest.roomWUnits/2, 
    PApplet.parseInt(P.y-initHeight)/60+Rtest.roomHUnits/2, PApplet.parseInt(P.y+initHeight/2)/60+Rtest.roomHUnits/2));
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
     Rtest.colCheckEnv2(F, PApplet.parseInt(F.x-initWidth)/60+Rtest.roomWUnits/2, PApplet.parseInt(F.x+initWidth)/60+Rtest.roomWUnits/2, 
    PApplet.parseInt(F.y-initHeight)/60+Rtest.roomHUnits/2, PApplet.parseInt(F.y+initHeight/2)/60+Rtest.roomHUnits/2);
     F.gameLogic(Player1);
     F.facingCheckAndChange(Player1);
     F.CollisionBox.x = F.x - F.CollisionBox.br/2;  F.CollisionBox.y = F.y;
       F.updatePos(Rtest.colCheckEnv2(F, PApplet.parseInt(F.x-initWidth)/60+Rtest.roomWUnits/2, PApplet.parseInt(F.x+initWidth)/60+Rtest.roomWUnits/2, 
    PApplet.parseInt(F.y-initHeight)/60+Rtest.roomHUnits/2, PApplet.parseInt(F.y+initHeight/2)/60+Rtest.roomHUnits/2));
  
   }  
   
     }
     
   }
  
  for(int i = VisEffectsList.size()-1; i >= 0; i--){
    if(VisEffectsList.get(i).exTimer <= 0){
      VisEffectsList.remove(i);
    }
  }
     
  }
  
      public void drawVisuals(){
        if(Rtest.Edit) return;
        if(frameFreeze <= 0){
  //centerX = initWidth/2.0;//dist(Player1.x, 0, Player2.x, 0)/2;
  float centerZ = 0;
  float centerY = 0;
  float centerZ2 = centerZ / tan(PI*30.0f / 180.0f);
  
   Camerabox.x = Player1.x + (initWidth/16) * Player1.dirMult;
   centerY = (initHeight - initHeight/4 - Player1.y);
  
   camera(Camerabox.x, initHeight/2.0f - centerY, (initHeight/2.0f+centerZ) / tan(PI*30.0f / 180.0f),
    Camerabox.x, initHeight/2.0f - centerY, 0,
    0, 1, 0);
    
  background(150);
    //StageBackground.drawBackground();
    Rtest.drawM0(PApplet.parseInt(Player1.x-initWidth)/60+Rtest.roomWUnits/2, PApplet.parseInt(Player1.x+initWidth)/60+Rtest.roomWUnits/2, 
    PApplet.parseInt(Player1.y-initHeight)/60+Rtest.roomHUnits/2, PApplet.parseInt(Player1.y+initHeight/2)/60+Rtest.roomHUnits/2);
    for(ColRect ce : Rtest.ColEbene){
      ce.draw();
    }
    
    fill(0);
    textSize(30);
    
    if(Player1.comboCount > 0){ text("COMBO:" + Player1.comboCount, PApplet.parseInt(Camerabox.x) - initWidth/2, 200 - PApplet.parseInt(centerY)); }
    //if(Player2.counterState){ text("COUNTER", int(Camerabox.x) - initWidth/2, 150 - int(centerY)); }
    //if( Player2.comboCount > 0){ text("COMBO:" + Player2.comboCount, int(Camerabox.x) + initWidth/4, 200 - int(centerY)); }
    if(Player1.counterState){ text("COUNTER", PApplet.parseInt(Camerabox.x) + initWidth/4, 150 - PApplet.parseInt(centerY)); }
    
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
  
  public void keyPressed(){   Rtest.keyPressed(); }
  public void keyReleased(){  Rtest.keyReleased(); 
            if(key == ' ' && !menuOn && !Rtest.Edit) menuOn = true;
    else if(key == ' ' && menuOn && !Rtest.Edit) menuOn = false;

}
  
}

class GS_Handler extends Gamestate{
  ArrayList<Gamestate> ToHandle;
  GS_Handler(ArrayList<Gamestate> ToHandle){
    this.ToHandle = ToHandle;
  }
    public void gameLogic(){
      for(int i = ToHandle.size()-1; 0 <= i; i--){
        if(ToHandle.get(i).deletMe){ ToHandle.remove(i);}
      }
    }
  public void drawVisuals(){ text("Only GS_Hadler loaded", initWidth/2, initHeight/2);}
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
   
  public void gameLogic(){
          camera(initWidth/2, initHeight/2.0f, (initHeight/2.0f) / tan(PI*30.0f / 180.0f),
    initWidth/2, initHeight/2.0f, 0,
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
  public void drawVisuals(){}
  
  public void keyPressed(){}
  public void keyReleased(){}
  
}

class TrainingMode extends Gamestate{
  Training_Menu Menu = new Training_Menu(); boolean menuOn = false;
  Gamestate CurGS = new F_2V2Fightmode();
    public void setup(){}
  public void gameLogic(){
    if(//menuOn && 
  CurGS instanceof F_2V2Fightmode){ Menu.inGameEffect(Player1, Player2); if(menuOn){return;} }  
    CurGS.gameLogic();
  }
  public void drawVisuals(){
    CurGS.drawVisuals();
    if(menuOn) menu(Player1, Player2);
  }
  public void keyPressed(){CurGS.keyPressed();}
  public void keyReleased(){
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
  
  public void menu(Fighter P1, Fighter P2){
    Menu.draw(P1, P2);
    if(Menu.Backbox.clicked) menuOn = false;
  }
  
}

class F_2V2Multiplayer extends Gamestate{
    Gamestate CurGS = new F_2V2Fightmode(); boolean menuOn = false; MP_Menu Menu = new MP_Menu();
    public void setup(){}
  public void gameLogic(){
        if(menuOn && CurGS instanceof F_2V2Fightmode){ Menu.inGameEffect(Player1, Player2); return;}  
    CurGS.gameLogic();
    if(Player1.curHP <= 0 || Player2.curHP <= 0) CurGameState = new F_Winscreen(this);
  }
  public void drawVisuals(){
    CurGS.drawVisuals();
    if(menuOn) menu(Player1, Player2);
  }
  public void keyPressed(){CurGS.keyPressed();}
  public void keyReleased(){
    CurGS.keyReleased();
        if(key == ' ' && CurGS instanceof F_2V2Fightmode && !menuOn) menuOn = true;
    else if(key == ' ' && CurGS instanceof F_2V2Fightmode && menuOn) menuOn = false;
}
  
  public void menu(Fighter P1, Fighter P2){
    Menu.draw(P1, P2);
    if(Menu.Backbox.clicked) menuOn = false;
  }
  
}

class F_2V2Fightmode extends Gamestate{
  
 // Fighter Player1, Player2;
  boolean searchDevices = false;
  
  public void gameLogic(){
     
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
    }
    if(l_Boxes2 != null){
    Player1.changeAction(l_P2Act);
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
  
  public void drawVisuals(){
        if(frameFreeze <= 0){
  //centerX = initWidth/2.0;//dist(Player1.x, 0, Player2.x, 0)/2;
  float centerZ = 0;
  float centerY = 0;
  float centerZ2 = centerZ / tan(PI*30.0f / 180.0f);
  
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
  
  
    camera(Camerabox.x, initHeight/2.0f - centerY, (initHeight/2.0f+centerZ) / tan(PI*30.0f / 180.0f),
    Camerabox.x, initHeight/2.0f - centerY, 0,
    0, 1, 0);
    
  background(150);
    StageBackground.drawBackground();
    
    fill(0);
    textSize(30);
    
    if(Player1.comboCount > 0){ text("COMBO:" + Player1.comboCount, PApplet.parseInt(Camerabox.x) - initWidth/2, 200 - PApplet.parseInt(centerY)); }
    if(Player2.counterState){ text("COUNTER", PApplet.parseInt(Camerabox.x) - initWidth/2, 150 - PApplet.parseInt(centerY)); }
    if( Player2.comboCount > 0){ text("COMBO:" + Player2.comboCount, PApplet.parseInt(Camerabox.x) + initWidth/4, 200 - PApplet.parseInt(centerY)); }
    if(Player1.counterState){ text("COUNTER", PApplet.parseInt(Camerabox.x) + initWidth/4, 150 - PApplet.parseInt(centerY)); }
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
  
  public void keyPressed(){  Player1.keyPressed();Player2.keyPressed();}
  public void keyReleased(){  Player1.keyReleased();Player2.keyReleased();
    if(key == 'P' && !searchDevices)
      searchDevices = true;
    else if(key == 'P' && searchDevices) searchDevices = false;

  }
 
}

class F_Winscreen extends Gamestate{
  Gamestate savedGS;
  F_Winscreen(Gamestate savedGS){ this.savedGS = savedGS;}
  public void gameLogic(){
         
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
  
  public void resetPlayers(){
        Player1.changeAction(Player1.Standing);
    Player2.changeAction(Player2.Standing);
    Player1.curHP = Player1.maxHP;
    Player2.curHP = Player2.maxHP;
    Player1.x = initWidth/4;
    Player2.x = initWidth-initWidth/4;
  }
  
  public void drawVisuals(){
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
  
  public void gameLogic(){
    editMode(EditFighter, EditFighter.CurAction);
  }
  public void drawVisuals(){}
  
  private int drawEditbar(int size, int curBoxState, int[] infBr, float y, float h){
      float l_xcord = 0; //int retCurBox = 0;
  for(int i = 0; i < size; i++){
        int f = color(255);
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
          pact.updFrameDataArr_float(i, PApplet.parseFloat(EditForceXBox.boxText), PApplet.parseFloat(EditForceYBox.boxText));
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
      EditPlayer.CurAction.whenToUpdBoxs[eModeCurCBox] = PApplet.parseInt(EditFrameDurBox.boxText);
      println("changed");
    }

  else if(click && recPointCol(mouseX, mouseY, PApplet.parseInt(EditPlayer.x)-300, PApplet.parseInt(EditPlayer.y)-400, 600, 600)){ 
    
    if(mouseButton == LEFT){
      switch(editItemId){
        case 0:
    EditPlayer.CurAction.HitBoxCollect.get(eModeCurCBox).add(
    new ColCircle(mouseX-EditPlayer.x, mouseY-EditPlayer.y, PApplet.parseInt(EditDiaBox.boxText), PApplet.parseInt(EditDiaBox2.boxText), PApplet.parseInt(EditForceXBox.boxText), PApplet.parseInt(EditForceYBox.boxText), -1));
    break;
        case 1:
    EditPlayer.CurAction.HurtBoxCollect.get(eModeCurCBox).add(new ColCircle(mouseX-EditPlayer.x, mouseY-EditPlayer.y, PApplet.parseInt(EditDiaBox.boxText), PApplet.parseInt(EditDiaBox2.boxText) ));
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
  
  public void keyReleased(){
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
    EAct.whenToUpdBoxs[0] = PApplet.parseInt(EditFrameDurBox.boxText);
    EAct.whenToUpdBoxs = int_copyArrToSmallSize(EAct.whenToUpdBoxs, EAct.HitBoxCollect.size());
    EAct.setForceAtDur = float_copyArrToSmallSize(EAct.setForceAtDur, EAct.HitBoxCollect.size());
    // TO DO: add same for force Array
  }
  else if(keyCode == RIGHT && EAct.HitBoxCollect.size() == EAct.whenToUpdBoxs.length){

    EAct.addAllLists( EAct.HitBoxCollect.size()-1 , PApplet.parseInt(EditFrameDurBox.boxText), 0, 0);
    println("addedList");
  }
  else if(keyCode == RIGHT && EAct.HitBoxCollect.size() < EAct.whenToUpdBoxs.length){
         EAct.addAllLists( EAct.HitBoxCollect.size()-1 , PApplet.parseInt(EditFrameDurBox.boxText), 0, 0);
  }

    if(keyCode == DOWN && editItemId > 0){
    editItemId--;
  }
  else if(keyCode == UP){
    editItemId++;
  } 

  }
  
  public void finishState(){ eModeTimer = 0; eModeCurCBox = 0; eModeCurCAnim = 0; }
  
}
class MenuElements{
}

class Training_Menu extends MenuElements{
  int mState = 0;
  MenuBox Movelistbox = new MenuBox(initWidth*0.75f, 50, 50, 200, "MOVELIST", 255), StateBox = new MenuBox(initWidth*0.75f, 100, 50, 200, "State", 255),
  recordBox = new MenuBox(initWidth*0.75f, 150, 50, 200, "Record", 255), BackToMain = new MenuBox(initWidth*0.75f, 200, 50, 200, "MAINMENU", 255), 
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
  
  public void draw(Fighter P1, Fighter P2){
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

public void inGameEffect(Fighter P1, Fighter P2){
  if(easyPlayBox.flicked){P1.easyMode = true; P2.easyMode = true;} else {P1.easyMode = false; P2.easyMode = false;};
  if(AIbox.flicked){P2.AI_Controlled = true;} else {P2.AI_Controlled = false;};
  if(OppTechBox.flicked && P2.CurAction == P2.softKD){P2.firstPressInp[4] = true; P2.inputs[4] = true;}

}

public String intArrToString(int[] arr){
  String s = "";
  for(int i = 0; i < arr.length; i++){ s += arr[i]; }
  return s;
}

public void moveList(Fighter F, float x){
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
  rect(x, y, breite, hoehe);
  textSize(40);
  textAlign(CENTER);
  fill(0);
  text(this.boxText,x+breite/2,y+hoehe/2);  
  
  if(mouseInBox()){
    blink(x + breite, y +hoehe/2);
  }
} 

public void menuBoxClick(){
  if(click && mouseInBox()){
    clicked = true;
  }else clicked = false;
}

public boolean mouseInBox(){
    float xScale = PApplet.parseFloat(width) / PApplet.parseFloat(initWidth), yScale = PApplet.parseFloat(height) / PApplet.parseFloat(initHeight);
  return x * xScale <= mouseX && mouseX <= (x + breite)*xScale && y * yScale <=mouseY && mouseY <= (y + hoehe)*yScale;
}

public void blink(float xb, float yb){
  float xC, yC;
 
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

class CharSelectKachel extends MenuBox{
  PImage BG, BigCharArt; Animation CharSprs;
  
  CharSelectKachel(float xmb, float ymb, float bmb, float hmb, PImage BG, Animation CharSprs, PImage BigCharArt){
    super(xmb, ymb, bmb, hmb);
    this.BG = BG; this.CharSprs = CharSprs; this.BigCharArt = BigCharArt;
  }
  
  public void displayMenuBox(){
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
  
  public void draw(){
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
     String name = "Stage"; 
  Stage(){
  }
  
  public void loadImages(){
  }
  
  public void drawBackground(){
  }
  
  public void drawForeground(){
  }
    
}

class TrainingStage extends Stage{
  PImage bg0; AudioPlayer bgmusic;
  
  TrainingStage(){
    loadImages();
    name = "Training";
  }
  
  public void loadImages(){
    bg0 = loadImage("Stages/TrainingsStage/TrainingStage.png");
    bgmusic = minim.loadFile("Soundeffekte/Bgmusic/Optionselectmenu.wav");
  }
  
    public void drawBackground(){
      imageMode(CORNER);
      image(bg0, 0-initWidth/2, -bg0.height + GROUNDHEIGHT +100);//bg0.height);
      bgmusic.play();
      if (bgmusic.position()>=bgmusic.length()) bgmusic.cue(0);
  }
  
}

class BergGrossStage extends Stage{
  PImage bg0, bg1, fg0; AudioPlayer bgmusic;
  
  BergGrossStage(){
    loadImages();
    name = "Giant Mountain";
  }
  
  public void loadImages(){
    bg0 = loadImage("Stages/BergGroßStage/BergGroßstage2.png");
    bg1 = loadImage("Stages/BergGroßStage/BergGroßstage1.png");
    //fg0 = loadImage("Stages/BergGroßStage/BergGroßstage0.png");
    bgmusic = minim.loadFile("Soundeffekte/Bgmusic/Std-HF-Battletheme.wav");
  }
  
    public void drawBackground(){
            imageMode(CENTER);
      image(bg0, Camerabox.x, Camerabox.y);
      imageMode(CORNER);
      image(bg1, 0-initWidth/2, -bg0.height + GROUNDHEIGHT +100);//bg0.height);
      bgmusic.play();
      if (bgmusic.position()>=bgmusic.length()) bgmusic.cue(0);
  }
  
}
static class Structs{
  private Structs(){}
}

class PlControl extends Structs{
    ControlDevice device = null; ControlButton LightBut, MediumBut, HeavyBut, RCBut, EMBut; ControlHat DPad; ControlSlider LStick_X, LStick_Y; boolean[] pInputs = new boolean[4];
  boolean[] inputs = new boolean[9]; //up , down, right, left, Roof, Mid, Base, utility(RC), Special;
  boolean[] firstPressInp = new boolean[inputs.length];
  boolean[] firstPressDia = {true, true, true, true};
  int[] inputChargeT = new int[inputs.length];
  char[] charinputs;
  
  PlControl(){device = null; charinputs = new char[]{'w', 's', 'd', 'a', 'j', 'i', 'o', 'p', 'n'};}
  PlControl(ControlDevice device){this.device = device; if(device != null)setConDevice(); charinputs = new char[]{'w', 's', 'd', 'a', 'j', 'i', 'o', 'p', 'n'};}
  PlControl(char[] charinputs){this.device = null; this.charinputs = charinputs;}
  PlControl(ControlDevice device, char[] charinputs){this.device = device; if(device != null)setConDevice(); this.charinputs = charinputs;}
  
    public void setConDevice(){
    DPad = device.getHat("DPAD");
    LightBut = device.getButton("LIGHT");
    MediumBut = device.getButton("MEDIUM"); 
    HeavyBut = device.getButton("HEAVY");
    RCBut = device.getButton("RC");
    EMBut = device.getButton("EM");
    LStick_X = device.getSlider("LSTICK-X"); 
    LStick_Y = device.getSlider("LSTICK-Y");
  }
  
   public void deviceInput(){

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
      inputs[8] = EMBut.pressed();
    }
    
    
  }
  
  public void searchDevice(){
       control.getDevices().clear();
      println(control.deviceListToText("") );
      println("searching for devices now");
        
         Player1.PlContr.device = null; 
         Player2.PlContr.device = null; 
         control = ControlIO.getInstance(p);
         Player1.PlContr.device = control.getMatchedDeviceSilent("HFcontroller"); 
         Player2.PlContr.device = control.getMatchedDeviceSilent("HFcontroller"); 
         if(Player1.PlContr.device != null) {//println( device.getName()+ " " + device.getPortTypeName() ); 
          Player1.PlContr.setConDevice(); println("connect");}
         if(Player2.PlContr.device != null) {//println( device.getName()+ " " + device.getPortTypeName() ); 
          Player2.PlContr.setConDevice(); println("connect");}
         
         println(control.deviceListToText("") );
  }
  
  
  public void draw(){
        //chargeinputs
    for(int i = 0; i < inputs.length; i++){
      if(inputs[i] && inputChargeT[i] <= 1000) inputChargeT[i]++;
      else if(inputChargeT[i] > 10) inputChargeT[i] -= inputChargeT[i]/10;
      else inputChargeT[i] = 0;     
    }
   //chargeinputs
   
   for(int i = 0; i < firstPressInp.length; i++){
      if(!inputs[i]) firstPressInp[i] = true;   
      else firstPressInp[i] = false;
    }
  
   for(int i = 0; i < pInputs.length; i++) pInputs[i] = inputs[i];
  }
  
  public void keyPressed(){ if(device == null)this.inputs = inputsKey(true, this.charinputs, this.inputs); }
  public void keyReleased(){ if(device == null)this.inputs = inputsKey(false, this.charinputs, this.inputs); }
}

class InputRecord extends Structs{
  
  // "0000 (Dir) 000 (AttButs) 0 (RC) : timeactive"
  ArrayList<String> InpRec = new ArrayList<String>();
  Fighter FtoRec = null;
  int state = 0; // 0: Nothing; 1: Record; 2: Play 
  int t_sameInputs = 0;
  int timer = 0, curCollumn = 0;
  int maxActiveTime = 60*60*10, activeTime = 0;
  boolean sideSens = false;
  
  InputRecord(){
  } 
  InputRecord(Fighter FtoRec){
    this.FtoRec = FtoRec;
  }
  InputRecord(Fighter FtoRec, int state, boolean sideSens, String... RecInpData){
    this.FtoRec = FtoRec; this.state = state; this.sideSens = sideSens;
    for(String s : RecInpData){
        InpRec.add(s);
    }   
    
  } 
  
  public void work(Fighter toRec){
    switch(state){
      case 0:
      break;
      case 1:
      recordInputs(toRec);
      break;
      case 2:
      playRec(toRec);
      break;
      default:
      break;
    }
  }
  
  public void reset(){
    t_sameInputs = 0; timer = 0; curCollumn = 0;
  }
  public void deleteRec(){
    InpRec.clear();
    reset();
    activeTime = 0;
  }
  
  public void genRecData(String datnam){
    JSONObject data = new JSONObject();
    data.setInt("RecLength", InpRec.size());
    int i = 0;
    for(String Str : InpRec){
      data.setString("cRec"+i, Str);
      i++;
    }
    
    saveJSONObject(data, datnam);
  }
  
  public void loadRecData(String datnam){
    JSONObject data = loadJSONObject(datnam);
    int imax = data.getInt("RecLength");
    for(int i = 0; i < imax; i++){
      InpRec.add( data.getString("cRec"+i) );

    }
  }
  
  public boolean recAtEnd(){
    return curCollumn >= InpRec.size()-1;
  }
  
  public void playRec(Fighter toRec){
    if(timer == 0 && curCollumn == 0){
      timer = PApplet.parseInt( InpRec.get(0).substring(toRec.inputs.length+1) );
    }
    
    for(int i = 0; i < toRec.inputs.length; i++){
      if(InpRec.get(curCollumn).charAt(i) == '1' ){

        toRec.inputs[i] = true;
        
      }
      else{toRec.inputs[i] = false;}
      
    }
    if(sideSens && toRec.dirMult == -1){
      boolean tmp = toRec.inputs[2]; toRec.inputs[2] = toRec.inputs[3]; toRec.inputs[3] = tmp;
    }
    
    timer--;
    
    if(timer <= 0 && curCollumn < InpRec.size()-1){
      curCollumn++;
      timer = PApplet.parseInt( InpRec.get(curCollumn).substring(toRec.inputs.length) );
    }
    
    
  }
  
  public void recordInputs(Fighter toRec){
    if(activeTime < maxActiveTime){

    String CurRecord = "";
    for(int i = 0; i < toRec.inputs.length; i++){
      if(toRec.inputs[i]){
        CurRecord += "1";
      }
      else{ CurRecord += "0"; }
    }
    
    if(InpRec.size() == 0){
      InpRec.add(CurRecord + ":1");
      t_sameInputs = 1;
    }
    else if(InpRec.size() > 0){
      String S1 = CurRecord.substring(0, toRec.inputs.length-1),
      S2 = InpRec.get(InpRec.size()-1).substring(0, toRec.inputs.length-1);
      
      if( S1.equals( S2 ) ){
        t_sameInputs++;
        InpRec.set(InpRec.size()-1, S1 + ":" + t_sameInputs);
      }
      else{
          InpRec.add(CurRecord + ":1");
          t_sameInputs = 1;
      }
      
    }
    
  }
  
  }
  
  public void printRec(){
    for(String Str : InpRec){ println(Str); }
  }
  
}

static class AnimControl extends Structs{
   private class AnimID{
    String SprsName = ""; PImage[] Sprs; int AnimSize = 0; int AnimObjCount = 0;
    AnimID(String SprsName, PImage[] Sprs, int AnimSize){
      this.SprsName = SprsName; this.Sprs = Sprs; this.AnimSize = AnimSize; this.AnimObjCount = 1;
    }
  }
    
  static private ArrayList<AnimID> AnimList = new ArrayList<AnimID>();
  
  public static void enterNewAnim(Animation Anim, int Animlength){
    //removeNotUsedAnim();
    String aDatnam = Anim.datnam;
    for(AnimID a : AnimList){
      
      if( a.SprsName.equals(aDatnam) && Animlength == a.AnimSize
      ){
        //println(a.SprsName + "Anim already listed, Size: " + a.AnimSize);
        Anim.Sprites = a.Sprs; a.AnimObjCount++;
        return;
      }
      
    }  

      PImage[] SprsToPass = Anim.loadSpr_Arr(Animlength);
      AnimList.add(new AnimControl().new AnimID(aDatnam, SprsToPass, Animlength));
      Anim.Sprites = SprsToPass;
      //println(aDatnam + "Anim newly loaded");
    
  }
  
  public static void removeNotUsedAnim(){
    for(int i = AnimList.size()-1; i >= 0; i--) if(AnimList.get(i).AnimObjCount <= 0) AnimList.remove(i);
  }

}

class Animation extends Structs{
  
  int curCollumn = 0;
  int timer = 0;
  
  int[] changeTimes;
  
  int X_coords, Y_coords; public float rot = 0.0f;
  
  int[] sprite_ID;
  
  PImage[] Sprites; String datnam = "none";
  
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
   this(changeTimes, X_coords, 0, Spr_Size_Arr, datnam);
  }
  
 Animation(int[] changeTimes, int X_coords, int Y_coords, int Spr_Size_Arr, String datnam){
    this.changeTimes = changeTimes;
    this.X_coords = X_coords;
    this.Y_coords = Y_coords;
    this.datnam = datnam;
    //println(datnam);
    //Sprites = loadSpr_Arr(Spr_Size_Arr);
     AnimControl.enterNewAnim(this, Spr_Size_Arr);
         //println("funkt");
  }
  
 Animation(int[] changeTimes, int X_coords, int Y_coords, PImage[] Sprites){
    this.changeTimes = changeTimes;
    this.X_coords = X_coords;
    this.Y_coords = Y_coords;
    this.Sprites = Sprites;
  }
  
  Animation(Animation toCopy){
    this.datnam = toCopy.datnam; //BUG: datnam standard "none" -> needed to be passed in copy constructor, otherwise bug in Animcontrol //IMPORTANT
    this.changeTimes = toCopy.changeTimes;
    this.X_coords = toCopy.X_coords;
    this.Y_coords = toCopy.Y_coords;
    this.Sprites = toCopy.Sprites;
    this.loop = toCopy.loop;
    AnimControl.enterNewAnim(toCopy, toCopy.changeTimes.length);
  }
  
  /*void finalize(){
    for(AnimControl.AnimID a : AnimControl.AnimList){
      if( a.Sprs == Sprites && a.AnimSize == changeTimes.length ){
        a.AnimObjCount--; println(a.SprsName+a.AnimObjCount); break;
      }
        }
  }*/
  
  public void handleAnim(){
    boolean incrTimer = true;
    if(curCollumn == 0 && timer <= 0){
      
    }
    else if( curCollumn < Sprites.length-1 && timer >= changeTimes[curCollumn]){
      curCollumn++;
      timer = 0;
    }
    else if( curCollumn >= Sprites.length-1 && timer >= changeTimes[curCollumn] && loop){
      curCollumn = 0;
      timer = 0;
      incrTimer = false;
    }
    
    if(incrTimer){
      timer++; //reaching integer limit if loop == false ?
    }
  }
  
  public void draw(float l_x, float l_y){
    pushMatrix();
    translate(l_x, l_y);
      rotate(rot);
      image( Sprites[curCollumn],
      X_coords, 
      Y_coords);   
    popMatrix();
  }
  
  public void Reset(){
    timer = 0;
    curCollumn = 0;
  }
  
  public PImage[] loadSpr_Arr(int Spr_Size_Arr){
      PImage[] SprsToRet = new PImage[Spr_Size_Arr];
    for(int i = 0; i < Spr_Size_Arr; i++){
      println(datnam + i + ".png");
      SprsToRet[i] = loadImage(datnam + i + ".png");
    }
    
    return SprsToRet;
  }
  
  
}

class Projectile extends Structs{
  final int MID = 0;
  final int HIGH = 1;
  final int LOW = 2;
  
  int addx = 0, addy = 0;
  float x, y;
  int br = 0, ho = 0;
  int dirMult = 1;
  
  float forcex, forcey;
  
  float m = 1.2f;
  
  int attKind = 3;
  int exTimer = 200;
  int hitStun = 10;
  int blockStun = 5;
  int damage = 10;
  
  boolean effByFric = false;
  boolean effByGrav = false;
  boolean destroyedByCol = false;
  boolean destroyedByHit = true, hitOpp = true;
  
  PImage Spr;
  Animation Anim = null;
  Animation destrEff = null;
  
  ColCircle HitBox;
  
  Projectile(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, boolean effByFric, boolean effByGrav, boolean destroyedByCol){
    this.x = x; this.y = y; this.br = br; this.ho = ho; this.forcex = forcex; this.forcey = forcey;
    this.m = m;this.exTimer = exTimer;this.hitStun = hitStun; this.blockStun = blockStun; this.damage = damage;
    this.effByFric = effByFric; this.effByGrav = effByGrav; this.destroyedByCol = destroyedByCol;
    this.HitBox = new ColCircle(0, 0, br, ho, forcex, forcey, exTimer);
        HitBox.x = x;
    HitBox.y = y;
    Spr = Proj_sprs[0];
    
  }
  
    Projectile(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit){
      this(x, y, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol); this.destroyedByHit = destroyedByHit;
    }
    Projectile(float x, float y, int addx, int addy, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, 
    boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit, boolean hitOpp){
      this(x, y, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit); this.hitOpp = hitOpp; this.addx = addx; this.addy = addy;
    }
    Projectile(float x, float y, int addx, int addy, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, 
    boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit, boolean hitOpp, int dirMult){
      this(x, y, addx, addy, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit, hitOpp);  this.dirMult = dirMult;
    }
    Projectile(Projectile P){
      this(P.x, P.y, P.addx, P.addy, P.br, P.ho, P.forcex, P.forcey, P.m, P.exTimer, P.hitStun, P.blockStun, P.damage, 
      P.effByFric, P.effByGrav, P.destroyedByCol, P.destroyedByHit, P.hitOpp, P.dirMult);
    }
    
 public Projectile copy(){return new Projectile(this);} //needs to be implemented in unterklassen to use overwritten function
 public void setXY(int mult, float x, float y){this.x = x + addx*mult;this.forcex *= mult; this.y = y + addy; this.dirMult = mult; this.HitBox.x = this.x; this.HitBox.y = this.y;};
 public void setAnims(Animation a, Animation d){ if( a != null && d != null) Anim = new Animation(a); destrEff = new Animation(d);};
  
 public void specialStuff(Fighter Pl, Fighter Opp){
   //println("Oberklasse bbb");
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
    x += forcex;//*dirMult;
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
    pushMatrix();
    translate( x, y);

    scale( dirMult, 1);
    imageMode(CENTER);
    if(Anim != null){
       image( Anim.Sprites[Anim.curCollumn], Anim.X_coords, Anim.Y_coords);
    }
    else{
      image(Spr, 0, 0);
    }
    popMatrix();
            fill(255, 0, 0);
      HitBox.draw(1);
        
  }
  
}

class WH_Oilpuddle extends Projectile{
  WH_Oilpuddle(float x, float y, int addx, int addy, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, 
    boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit, boolean hitOpp){
      super(x, y, addx, addy, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit, hitOpp);
    }
    WH_Oilpuddle(Projectile P){
      super(P.x, P.y, P.addx, P.addy, P.br, P.ho, P.forcex, P.forcey, P.m, P.exTimer, P.hitStun, P.blockStun, P.damage, P.effByFric, P.effByGrav, P.destroyedByCol, P.destroyedByHit, P.hitOpp);}
      
    public Projectile copy(){return new WH_Oilpuddle(this);}

    public void specialStuff(Fighter Pl, Fighter Opp){   
      checkInOil(Pl); checkInOil(Opp);

    }
    
    public void checkInOil(Fighter F){
      //ColRect Fc = F.CollisionBox;
      if( recPointCol(F.x, F.y, this.x-br/2, this.y, this.br, this.ho) && new Grounded().cond(F, F) ){
        F.Force.x += F.Force.x * 0.08f;  
      }

    }
    
}

class PH_Apple extends Projectile{
  F_PHaus F;
  
    PH_Apple(float x, float y, int br, int ho, float forcex, float forcey, float m, int exTimer, int hitStun, int blockStun, int damage, 
    boolean effByFric, boolean effByGrav, boolean destroyedByCol, boolean destroyedByHit, F_PHaus F){
      super(x, y, br, ho, forcex, forcey, m, exTimer, hitStun, blockStun, damage, effByFric, effByGrav, destroyedByCol, destroyedByHit);
      this.F = F;
    }
  
   public void specialStuff(Fighter Pl, Fighter Opp){
     println("booohh");
     for(ColCircle c : F.HurtBoxes){
       if(c.compare(HitBox, F.dirMult, 1)){
         F.curFartMeter += 30;
         exTimer = 0;
       }
     }
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

abstract class TimedScreenEff extends Structs{
    float x, y = 0;
  int exTimer = 30;
  
  public void draw(){
        if(exTimer > 0) exTimer--;
  }

}

class VisualEffect extends TimedScreenEff{
  float scale = 0; int xdirMult = 1;
  Animation Anim;
  
  
  VisualEffect(float x, float y, Animation Anim, float scale){
    this.Anim = new Animation(Anim);
    this.Anim.Reset();
    this.x = x;
    this.y = y;
    this.scale = scale;
    this.exTimer = sumOfArr(Anim.changeTimes);
  }
  VisualEffect(float x, float y, Animation Anim, float scale, int xdirMult){
    this(x, y, Anim, scale); this.xdirMult = xdirMult;
  }
  
  public void draw(){
    Anim.handleAnim();
    pushMatrix();
    translate(x,y);
    imageMode(CENTER);
    if(scale > 1){
    scale((scale*xdirMult+1)*0.6f, scale+1);
    }
    image(Anim.Sprites[Anim.curCollumn], 0, 0);
    popMatrix();
    
    super.draw();
    
  }
  
}

class PopUpMssg extends TimedScreenEff{
  String Text = "POP-UP"; int textSize = 20; int textF = 0;
  PopUpMssg(float x, float y, String Text, int textSize, int textF){
    this.x = x; this.y = y;
    this.Text = Text; this.textSize = textSize; this.textF = textF;
  }
  public void draw(){
    textSize(textSize);
    fill(textF);
    textMode(CENTER);
    text(Text, x, y);
    super.draw();
  }
  
}

class GridCursor<T extends MenuBox> extends Structs{
  T[][] Grid;
  int xGrid = 0, yGrid = 0, clickBut = 0, backBut = 0;
  PlControl Con; boolean clicked = false, backClick = false;
  
  GridCursor(T[][] Grid, int xGrid, int yGrid, PlControl Con, int clickBut, int backBut){
    this.Grid = Grid; this.xGrid = xGrid; this.yGrid = yGrid; 
    this.Con = Con; this.clickBut = clickBut; this.backBut = backBut;
  }
  
  public void draw(int lx, int ly){
    T m = Grid[yGrid][xGrid];
    fill(0, 0, 0, 0);
    ellipse(m.x, m.y, 100, 100);
  }
  
  public T CurMB(){return Grid[yGrid][xGrid];}
  
  public void Logic(){
    if(Con.firstPressInp[0] && Con.inputs[0] && !checkOutRange(xGrid, yGrid-1)) yGrid-=1;
    if(Con.firstPressInp[1] && Con.inputs[1] && !checkOutRange(xGrid, yGrid+1)) yGrid+=1;
    if(Con.firstPressInp[2] && Con.inputs[2] && !checkOutRange(xGrid+1, yGrid)) xGrid+=1;
    if(Con.firstPressInp[3] && Con.inputs[3] && !checkOutRange(xGrid-1, yGrid)) xGrid-=1;
    if(Con.firstPressInp[clickBut] && Con.inputs[clickBut]){Grid[yGrid][xGrid].clicked = true; clicked = true;}
    else clicked = false;
    if(Con.firstPressInp[backBut] && Con.inputs[backBut]){backClick = true;}
    else backClick = false;
  }
  
  public boolean checkOutRange(int lx, int ly){
    if(lx < 0 || ly < 0 || lx >= Grid[yGrid].length || ly >= Grid.length) return true;
    if(ly > yGrid && lx < Grid[yGrid].length){ if(lx > Grid[yGrid+1].length-1) return true; }
    if(ly < yGrid && lx < Grid[yGrid].length){ if(lx > Grid[yGrid-1].length-1) return true; }
    if(Grid[ly][lx] == null) return true;
    return false;
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

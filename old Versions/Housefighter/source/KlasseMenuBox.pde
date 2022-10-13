
class MenuBox{
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
  rect(x,y, breite, hoehe);
  textSize(40);
  textAlign(CENTER);
  fill(0);
  text(this.boxText,x+breite/2,y+hoehe/2);  
  
  if(x <= mouseX && mouseX <= x + breite && y <=mouseY &&mouseY <= y + hoehe){
    blink(x + breite, y +hoehe/2);
  }
} 

void menuBoxClick(){
  if(click && x <= mouseX && mouseX <= x + breite && y <=mouseY &&mouseY <= y + hoehe){
    clicked = true;
  }else clicked = false;
}

void blink(float xb, float yb){
  float xC;
  float yC;
 
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




class EintragBox extends MenuBox{
  int count = 0;
  int maxCount;
  
  EintragBox(float xmb, float ymb, float bmb, float hmb, int maxCount){
    super(xmb, ymb, bmb, hmb);
    this.maxCount = 30;
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

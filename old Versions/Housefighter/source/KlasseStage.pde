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
  
  void loadImages(){
    for(int i = 0; i < plLine; i++){
      hinterSchichten[i] = loadImage("Stages/" + datnam + i + ".png");
    }
    for(int i = 0; i < stageDeepness-plLine; i++){
      vorSchichten[i] = loadImage("Stages/" + datnam + (plLine+ i) + ".png");
    }
  }
  
  void drawBackground(){
    imageMode(CENTER);
    for(int i = 0; i < 1//plLine
    ; i++){
      PImage p = hinterSchichten[i];
      image(p, p.width/4 - (Camerabox.x-initWidth/4)*0.05*i, 0);
    }
  }
  
  void drawForeground(){
        imageMode(CENTER);
    for(int i = 0; i < stageDeepness-plLine; i++){
      PImage p = vorSchichten[i];
      image(p, p.width/4, 0);
    }
  }
  
  
}

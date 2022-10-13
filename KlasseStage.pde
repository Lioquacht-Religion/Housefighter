class Stage{
     String name = "Stage"; 
  Stage(){
  }
  
  void loadImages(){
  }
  
  void drawBackground(){
  }
  
  void drawForeground(){
  }
    
}

class TrainingStage extends Stage{
  PImage bg0; AudioPlayer bgmusic;
  
  TrainingStage(){
    loadImages();
    name = "Training";
  }
  
  void loadImages(){
    bg0 = loadImage("Stages/TrainingsStage/TrainingStage.png");
    bgmusic = minim.loadFile("Soundeffekte/Bgmusic/Optionselectmenu.wav");
  }
  
    void drawBackground(){
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
  
  void loadImages(){
    bg0 = loadImage("Stages/BergGroßStage/BergGroßstage2.png");
    bg1 = loadImage("Stages/BergGroßStage/BergGroßstage1.png");
    //fg0 = loadImage("Stages/BergGroßStage/BergGroßstage0.png");
    bgmusic = minim.loadFile("Soundeffekte/Bgmusic/Std-HF-Battletheme.wav");
  }
  
    void drawBackground(){
            imageMode(CENTER);
      image(bg0, Camerabox.x, Camerabox.y);
      imageMode(CORNER);
      image(bg1, 0-initWidth/2, -bg0.height + GROUNDHEIGHT +100);//bg0.height);
      bgmusic.play();
      if (bgmusic.position()>=bgmusic.length()) bgmusic.cue(0);
  }
  
}

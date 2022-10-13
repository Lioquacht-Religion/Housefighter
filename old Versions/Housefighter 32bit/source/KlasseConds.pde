class Condition{
  Condition(){}
  
  boolean cond(Fighter Pl, Fighter Opp){
    return true;
  }
}

class dirCombCond extends Condition{
  int[] motion = {5};
  dirCombCond(int[] motion){this.motion = motion;}
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.compareBufferWithCombAtt(motion);
  }
}

class ButCond extends Condition{
  int ButIndex = 0;
  ButCond(int ButIndex){ this.ButIndex = ButIndex;}
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.inputs[ButIndex];
  }
}

class fPButCond extends ButCond{
  fPButCond(int ButIndex){ super(ButIndex);}
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.inputs[ButIndex] && Pl.firstPressInp[ButIndex];
  }
}

class dirHorFPButC extends ButCond{ // for dir CommandNormals
  int holdBut = 0;
  dirHorFPButC(int ButIndex, int holdBut){ super(ButIndex); this.holdBut = holdBut;}
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.inputs[ButIndex] && Pl.firstPressInp[ButIndex] && Pl.inputs[holdBut];
  }
}

class comfPButC extends ButCond{
  int[] motion = {5};
  comfPButC(int ButIndex, int[] motion){ super(ButIndex); this.motion = motion;}
  boolean cond(Fighter Pl, Fighter Opp){
    return Pl.compareBufferWithCombAtt(motion) && Pl.inputs[ButIndex] && Pl.firstPressInp[ButIndex];
  }
}

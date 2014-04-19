//
// AgtionSet.java
// Nubot Simulator
//
// Created by David Chavez on 4/2/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import java.util.ArrayList;
import java.util.Random;

public class AgtionSet extends ArrayList<Agtion> {
    public Agtion selectArbitrary() {
        Random rand = new Random();
        int index = rand.nextInt(this.size());

        return this.get(index);
    }
}
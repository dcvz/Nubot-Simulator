//
// ActionSet.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import java.util.ArrayList;
import java.util.Random;

// list of possible actions
public class ActionSet extends ArrayList<Action> {
    public Action selectArbitrary() {
        Random rand = new Random();
        int index = rand.nextInt(this.size());

        Action ret = this.get(index);
        this.remove(ret);

        return ret;
    }
}
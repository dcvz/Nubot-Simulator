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
public class ActionSet extends ArrayList<Action>
{
    // Method to add and action to our ActionSet.
    public void addAction(Action a)
    {
        add(a);
    }

    // Returns a randomly selected Action from our ActionSet.
    public Action selectRandom()
    {
        if (size() > 0)
        {
            // Get random integer
            Random generator = new Random();
            int randomIndex = generator.nextInt(size());

            // Get action at that random integer.
            Action chosen = get(randomIndex);

            // Make sure to remove it, in case this action cannot be made.
            remove(randomIndex);

            // Return the random action.
            return chosen;
        }

        return null;
    }
}

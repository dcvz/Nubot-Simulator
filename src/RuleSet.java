//
// RuleSet.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import org.javatuples.*;

public class RuleSet extends HashMap<Quartet<String, String, Byte, Byte>, ArrayList<Quartet<String, String, Byte, Byte>>>
{
    public void addRule(Rule r)
    {
       Quartet<String, String, Byte, Byte> key = r.getInputQuartet();
       if(this.containsKey(key))
       {
           ArrayList<Quartet<String, String, Byte, Byte>> outputList = this.get(key);
           outputList.add(r.getOutputQuartet());
       }
        else
       {
           ArrayList<Quartet<String,String, Byte, Byte>> outputList = new ArrayList<Quartet<String, String, Byte, Byte>>();
           outputList.add(r.getOutputQuartet());
           this.put(key, outputList);
       }
    }
}
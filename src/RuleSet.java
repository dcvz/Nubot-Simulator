//
// RuleSet.java
// Nubot Simulator
//
// Created by David Chavez on 4/1/14.
// Copyright (c) 2014 Algorithmic Self-Assembly Research Group. All rights reserved.
//

import org.javatuples.Quartet;

import java.util.ArrayList;
import java.util.HashMap;

public class RuleSet extends HashMap<Quartet<String, String, Byte, Byte>, ArrayList<Rule>> {
    public void addRule(Rule r) {
        Quartet<String, String, Byte, Byte> key = r.getInputQuartet();
        if (this.containsKey(key)) {
            ArrayList<Rule> rules = this.get(key);
            rules.add(r);
        } else {
            ArrayList<Rule> rules = new ArrayList<Rule>();
            rules.add(r);
            this.put(key, rules);
        }
    }
}
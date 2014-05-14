###Nubot Simulator
======
The Nubot Simulator is a tool built to help researchers model their Nubot configurations and rulesets. This simulator has a number of features including but not limited to:

* Video Export
* Simulation Speed Changes
* Configuration Editing

###Using the Simulator
======
You are not allowed to start the simulation until both the configuration and ruleset files are loaded. However, in the case of testing a system that uses only agitation, you may simulate as long as a configuration is loaded and agitation is turned on.

####Loading a Configuration
======
One of the first things you'll want to do in order to simulate a system is load the seed configuration file. The simulator uses its own format and extension.

The configuration file format ends with the .conf extension and looks something like this:

```
States:
x, y, state

Bonds:
M1.x, M1.y, M2.x, M2.y, bondTYPE
```
Each monomer is specified under the 'States:' directive and consists of: the x coordinate, y coordinate and state.

Bonds between monomers are specified under the 'Bonds:' directive following the monomer declarations. Bond declaration consists of: the x & y coordinate of the first monomer, the x & y coordinate of the second monomer, followed by the type of bond between them.

Bond Types: 0 - null, 1 - rigid, 2 - flexible.

Example: [wiggles.conf]("http://faculty.utpa.edu/orgs/asarg/examples/wiggles.conf")

####Loading a Ruleset
======
Before you can start a simulation you must load a configuration and, unless you are testing agitation, a ruleset. The simulator uses its own format and extension.

The ruleset file format ends with the .rules extension and more closely follows the specifications presented in the model for interactions:

```
M1.state M2.state bondTYPE Direction M1'.state M2'.state bondTYPE' Direction'
```

Directions: N, NE, E, SE, S, SW, W, NW

Example: [wiggles.rules]("http://faculty.utpa.edu/orgs/asarg/examples/wiggles.rules")

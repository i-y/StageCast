Script Input File Formats {#script_inpt}
=========================

[TOC]

The operations of the modeling system are divided across two distinct portions, the main program programmed in Java and the supporting scripts which are programmed in R. In order to communicate to the R scripts, the main program outputs information files in a format R can easily read in. The format is a tab-delineated table with a header line. The R script is then called on the command line and passed the location of the instruction file as an argument. The R script then opens the file, reads in the table, and uses the content to run the requested operations. As output, the R script will either generate figures or an appropriate XML file as described in the @ref data_file page.

@note There is more detailed documentation inside the R scripts themselves.

Model {#model_r_inpt}
=====

This is the format of the file which is passed to the script `model.r`. It is rather small as it only needs to generate the basic model and does not contain extra options. The output file name is `modelInput.txt` and is stored in the “./Temp” directory.

The file format is:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
tag	data
weather Weather dataset location
species	Species 1 dataset location
species	Species 2 dataset location
...
species	Species N dataset location
stageMap    F0:G0
stageMap    F1:G1
...
stageMap    FN:GN
saveLoc	Output location
optim   Optimization function
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Statistics {#stats_r_inpt}
==========

This is the format of the file which is passed to the script `stats.r`. It is longer than the file passed to `model.r` because it requires more input data and options. The output file name is `statsInput.txt` and is stored in the “./Temp” directory.

The file format is:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
tag	data
weather	Weather dataset location
species	Species 1 dataset location
species	Species 2 dataset location
...
species	Species N dataset location
stageMap    F0:G0
stageMap    F1:G1
...
stageMap    FN:GN
saveLoc	Output location
iter	Calculation iterations
alpha	Alpha value
params	Parameter 1 value
params	Parameter 2 value
...
params	Parameter N value
AvalMat	A value setting
Astar	Astar setting
Vstar	Vstar setting
GGstar	GGstar setting
optim   Optimization function
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 - The `iter` tag holds how many times the statistical calculations are to be run on the data.
 - The `params` tags list the model parameters calculated previously by `model.r`.
 - The `alpha` tag holds the alpha value to use during statistical calculation.
 - The `AvalMat` tag value controls if the A value matrix is constructed.              0 = no, 1 = yes. 
 - The `Astar` tag value controls if the A star value matrix is constructed.           0 = no, 1 = yes. 
 - The `Vstar` tag value controls if the V star value matrix is constructed.           0 = no, 1 = yes. 
 - The `GGstar` tag value controls if the G-squared star value matrix is constructed.  0 = no, 1 = yes. 

Graphing {#graph_r_inpt}
========

This is the format of the while which is passed to the script `graph.r`. It is the longest of the three script input files because the graphing program accepts a sizable number of options which must be passed to the script. The output file name is `graphInput.txt` and is stored in the “./Temp” directory.

The file format is:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
tag	data
weather	Weather dataset location
species	Species 1 dataset location
species	Species 2 dataset location
...
species	Species N dataset location
stage	Stage 1 name
stage	Stage 2 name
...
stage	Stage 3 name
saveLoc	Output location
params	Parameter 1 value
params	Parameter 2 value
...
params	Parameter N value
logLike	Log likelihood
stageMap    F0:G0
stageMap    F1:G1
...
stageMap    FN:GN
log3d	3D logarithm setting
log2d	2D logarithm setting
expProp	Proportion graph setting
compRaw	Raw data vs model line graph setting
together	Stage graph setting
combined	Stage graph setting
log2dinterval	2D logarithm interval value
log2doffset	2D logarithm offset value
log2dsample	2D logarithm sample size value
logPdf3DShigh	3D logarithm high S limit value
logPdf3DSlow	3D logarithm low S limit value
logPdf3DThigh	3D logarithm high T limit value
logPdf3DTlow	3D logarithm low T limit value
expProphigh	Proportion graph high limit
expProplow	Proportion graph low limit
compRawhigh	Comparative graph high limit
compRawlow	Comparative graph low limit
combHigh	Combined graph high limit
combLow	Combined graph low limit
width	Figure width
height	Figure height
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 - The `log3d` tag controls if the 3D logarithm graph is constructed.
 - The `log2d` tag controls if the 2D logarithm graph is constructed.
 - The `expProp` tag controls if the expected proportion graph is constructed.
 - The `compRaw` tag controls if the raw data vs modeled result graph is constructed.
 - The `together` tag controls if the individual stage graphs should all be on one figure or if each stage graph should get its own figure.
 - The `combined` tag controls if the stages should be graphed against the data points on a single figure.
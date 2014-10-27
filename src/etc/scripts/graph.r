# This software is in the public domain. It was created by employees of the 
# United States Department of Agriculture as part of their official duties. It 
# is free and open for use and modification for any purpose without restriction 
# or fee. 
#
#
#                             ***Disclaimer***
#
# This is a free and open source software package provided by the Agricultural 
# Research Service of the United States Department of Agriculture (USDA ARS) 
# in the hopes that it will advance the common interest.
#
# ARS MAKES NO REPRESENTATION NOR EXTENDS ANY WARRANTIES OF ANY KIND, EITHER 
# EXPRESS OR IMPLIED, OF MERCHANTABILITY OR FITNESS OF THE TECHNOLOGY IN THIS 
# PROGRAM FOR ANY PARTICULAR PURPOSE, OR THAT THE USE OF THIS TECHNOLOGY WILL 
# NOT INFRINGE ANY PATENT, COPYRIGHT, TRADEMARK, OR OTHER INTELLECTUAL 
# PROPERTY RIGHTS, OR ANY OTHER EXPRESS OR IMPLIED WARRANTIES.

rm(list=ls(all=TRUE))

#-----------
# Functions
#-----------

#
# Function to calculate accumulated degree days using the double sine method.
#
# Note: assumes input in Celsius
#
# Takes:
#   x     - name of temperature data file
#   upper - high bound
#   lower - low bound
#
# Returns: Vector of time values expressed in degree days.
#
doubleSine = function(x, upper, lower){
   degreedata = read.table(x,header=FALSE,sep="\t")
   attach(degreedata)
   dmat = cbind(degreedata[2:3]) # Load the high/low information.
   detach(degreedata)
   retval = (1:nrow(dmat))*0
   for(i in 1:nrow(dmat)){
      firstLow = dmat[i,2]
	  high =dmat[i,1]
      firstAmp = ((high - firstLow)/2)
      firstAdj = firstAmp + firstLow - lower # Adjustment so the data is above or below the x-axis by as much as it is above or below the lower bound.
	  if(i < nrow(dmat)){
	     secondLow = dmat[i+1,2]
	  }else{
	     secondLow = firstLow
	  }
      secondAmp = ((high - secondLow)/2)
      secondAdj = secondAmp + secondLow - lower # Adjustment so the data is above or below the x-axis by as much as it is above or below the lower bound.
	  if(firstLow < lower){ # If the first low is below the minimum, find the area under the curve where the curve is above the minimum. 
	     int = function(x){firstAdj + firstAmp*sin(2*pi*x - (pi/2))}
		 # Find the intersections with the x-axis. 
		 # This would be the axis y = minimum but the curve has already been adjusted to a position relative to the x-axis rather than the minimum
         y1 = optimise(f=function(x){((int(x)) - 0)^2}, c(0,.5), maximum=FALSE)$minimum # Intersection 1
         y2 = optimise(f=function(x){((int(x)) - 0)^2}, c(.5,1), maximum=FALSE)$minimum # Intersection 2
		 firstArea = integrate(f=int, lower=y1, upper=y2)$value                         # The area under the parts of the curve above the x-axis.
	  }else{
	     firstArea = firstAdj # If the curve is above the x-axis, then the adjusted value is its area.
	  }
	  if(secondLow < lower){ # If the second low is below the minimum, find the area under the curve where the curve is above the minimum. 
	     int = function(x){secondAdj + secondAmp*sin(2*pi*x - (pi/2))}
		 # Find the intersections with the x-axis. 
		 # This would be the axis y = minimum but the curve has already been adjusted to a position relative to the x-axis rather than the minimum.
         y1 = optimise(f=function(x){((int(x)) - 0)^2}, c(0,.5), maximum=FALSE)$minimum # Intersection 1
         y2 = optimise(f=function(x){((int(x)) - 0)^2}, c(.5,1), maximum=FALSE)$minimum # Intersection 2
		 secondArea = integrate(f=int, lower=y1, upper=y2)$value                        # The area under the parts of the curve above the x-axis.
	  }else{
	     secondArea = secondAdj # If the curve is above the x-axis, then the adjusted value is its area.
	  }
	  if(high > upper){ # If the high value is above the maximum, find the area of the curve above the maximum and subtract it from the whole area,
	     upperadj = upper - lower # Adjust the upper bound down to be in line with the rest of the curve.
		 # Find the intersections for the first sine wave.
         x1a = optimise(f=function(x){((firstAdj + firstAmp*sin(2*pi*x - (pi/2))) - upperadj)^2}, c(0,.5), maximum=FALSE)$minimum
         x1b = optimise(f=function(x){((firstAdj + firstAmp*sin(2*pi*x - (pi/2))) - upperadj)^2}, c(.5,1), maximum=FALSE)$minimum
         firstIntegrand = function(x) {(firstAdj-upperadj) + firstAmp*sin(2*pi*x - (pi/2))}
		 # Find the intersections for the second sine wave.
         x2a = optimise(f=function(x){((secondAdj + secondAmp*sin(2*pi*x - (pi/2))) - upperadj)^2}, c(0,.5), maximum=FALSE)$minimum
         x2b = optimise(f=function(x){((secondAdj + secondAmp*sin(2*pi*x - (pi/2))) - upperadj)^2}, c(.5,1), maximum=FALSE)$minimum
         secondIntegrand = function(x) {(secondAdj-upperadj) + secondAmp*sin(2*pi*x - (pi/2))}
		 # Find the new areas.
         firstArea = firstArea - integrate(f=firstIntegrand, lower=x1a, upper=x1b)$value
		 secondArea = secondArea - integrate(f=secondIntegrand, lower=x2a, upper=x2b)$value
	  }
	  retval[i] = (firstArea/2)+(secondArea/2) # Find the total estimated area by adding one half of each wave together.
	  if(retval[i] < 0){
	     retval[i] = 0 # Keep any negative values from being recorded.
	  }
   }
   for(i in 2:nrow(dmat)){
      retval[i] = retval[i] + retval[i-1] # Sum the degree days for final output.
   }
   return(retval)
}

#
# Function used to calculate the probability density of a given dataset. 
#
# Takes:
#   s  - Input data array.
#   tt - Vector containing time values in degree days.
#
# Returns: Probability density function.
#
findPDF = function(s, tt){
   exp((s - tt)/sqrt(param[length(param)]*tt))/(sqrt(param[length(param)]*tt)*((1+exp((s - tt)/sqrt(param[length(param)]*tt)))^2))
}

#
# Function to find the probability of an organism being in a given instar relative to time.
#
# Takes:
#   A     - Vector holding the model parameters.
#   i     - Instar to find the probability for
#   dateT - Time vector. May be in either Julian or degree days.
#
# Returns: Calculated probability of the instar at the given time.
# 
P_ij = function(A, i, dateT){
   BB = A[length(A)]
   if(i == 1){
	  return(1/(1+exp((-(A[i] - dateT))/(sqrt(BB * dateT)))))
   }
   else if(i == (length(A))){
   	  return(1 - (1/(1+exp((-(A[i - 1] - dateT))/(sqrt(BB * dateT))))))
   }
   else{
      return((1/(1+exp(-((A[i] - dateT))/(sqrt(BB * dateT))))) - (1/(1+exp(-((A[i - 1] - dateT))/(sqrt(BB * dateT))))))
   } 
}

#
# Function takes the array holding the per-stage phenology data and collapses stages in to 
# each other based on the user's inputs.
#
# The mapping is done using two numbers, X and Y, in a string of the format "X:Y"
#	The X number is the original index of a given stage
#	The Y number is the index the stage information should be added to for the final output
#	If a given index is not collapsed into another one the map takes the form of "X:X"
#
# Takes:
#   Original - Array holding the unmodified phenology information
#   stageMap - vector of strings holding the map from one stage to another.
#
# Returns: Array with the number of columns equal to or less than the number of columns in 
# the input array.
#
collapse = function(original,stageMap) {
  stageMap = strsplit(stageMap, ":")
  stageMap = unlist(stageMap)

  stageTarget = strtoi(stageMap[seq(2,length(stageMap),2)],0L)
  stageTargetUnique = unique(stageTarget)
  stageTargetLen = length(stageTargetUnique)

  stageSource = strtoi(stageMap[seq(1,length(stageMap),2)],0L)
  stageSourceLen = length(unique(stageSource))

  ret = matrix(0, nrow(original), stageTargetLen)

  for(i in 1:ncol(original)) {
    target = match(stageTarget[i],stageTargetUnique)
    ret[,target] = ret[,target] + original[,stageSource[i] + 1]
  }
  return(ret)
}

#
# Function uses the stage map to determine the appropriate labels for the figures should be. 
# If a stage is the result of multiple stages being collapsed in to each other its label will 
# take the form "Name1/Name2/.../NameN"
#
# Takes:
#   Original - Vector of strings holding the unmodified list of stage names
#   stageMap - vector of strings holding the map from one stage to another.
#
# Returns: Vector of length equal to or less than the length of the input name vector.
#
collapseStageNames = function(original,stageMap) {
  stageMap = strsplit(stageMap, ":")
  stageMap = unlist(stageMap)
  
  stageTarget = strtoi(stageMap[seq(2,length(stageMap),2)],0L)
  stageTargetUnique = unique(stageTarget)
  stageTargetLen = length(stageTargetUnique)

  stageSource = strtoi(stageMap[seq(1,length(stageMap),2)],0L)
  stageSourceLen = length(unique(stageSource))
  
  ret = rep("",stageTargetLen)
  
  for(i in 1:length(original)) {
	target = match(stageTarget[i],stageTargetUnique)
	if(ret[target] == "" ) {
		ret[target] = original[stageSource[i] + 1]
	} else {
		ret[target] = paste(ret[target],original[stageSource[i] + 1],sep="/")
	}
  }
  return(ret)
}

#------------
# Read Input
#------------

# The program takes one argument from the command line, the location of the config file to be used.
args = commandArgs(trailingOnly = TRUE)
settings = args[1]

# Load the configuration file. This file contains all the input information and user-chosen options needed to graph.
# For yes/no options 0 = no and 1 = yes.
config = read.table(settings,header=TRUE,stringsAsFactors=FALSE,sep="\t")
weather = config$data[as.character(config$tag) == as.character("weather")]                         # Location of the weather file to use for graphing. 
species = config$data[as.character(config$tag) == as.character("species")]                         # Vector holding the species files used for graphing.
saveLoc = config$data[as.character(config$tag) == as.character("saveLoc")]                         # Location the figures should be saved to so that the calling program can find them.
params = as.numeric(config$data[as.character(config$tag) == as.character("params")])               # Vector holding the model parameters.
logLike = as.numeric(config$data[as.character(config$tag) == as.character("logLike")])             # The model's log likelihood.
logPdf3D = as.numeric(config$data[as.character(config$tag) == as.character("log3d")])              # Whether the script should create a 3D log PDF graph.
logPdf3DShigh = as.numeric(config$data[as.character(config$tag) == as.character("logPdf3DShigh")]) # The high range along the s-axis of 3D log PDF.
logPdf3DSlow = as.numeric(config$data[as.character(config$tag) == as.character("logPdf3DSlow")])   # The low range along the s-axis of 3D log PDF.
logPdf3DThigh = as.numeric(config$data[as.character(config$tag) == as.character("logPdf3DThigh")]) # The high range along the t-axis of 3D log PDF.
logPdf3DTlow = as.numeric(config$data[as.character(config$tag) == as.character("logPdf3DTlow")])   # The low range along the t-axis of 3D log PDF.
logPdf2D = as.numeric(config$data[as.character(config$tag) == as.character("log2d")])              # Whether the script should create a 2D log PDF graph.
log2dsample = as.numeric(config$data[as.character(config$tag) == as.character("log2dsample")])     # The number of samples to graph on the 2D log PDF.
log2dinterval = as.numeric(config$data[as.character(config$tag) == as.character("log2dinterval")]) # The interval between samples in the 2D log PDF.
log2doffset = as.numeric(config$data[as.character(config$tag) == as.character("log2doffset")])     # The offset of the first sample in the 2D log PDF.
exProp = as.numeric(config$data[as.character(config$tag) == as.character("expProp")])              # Whether the script should create an expected proportion graph.
expProphigh = as.numeric(config$data[as.character(config$tag) == as.character("expProphigh")])     # The high range for expected proportion graph.
expProplow = as.numeric(config$data[as.character(config$tag) == as.character("expProplow")])       # The low range for expected proportion graph.
compareRaw = as.numeric(config$data[as.character(config$tag) == as.character("compRaw")])          # Whether a graph should be created plotting the input data against the prediction lines.
compRawhigh = as.numeric(config$data[as.character(config$tag) == as.character("compRawhigh")])     # The high range for raw data vs modeled result graph.
compRawlow = as.numeric(config$data[as.character(config$tag) == as.character("compRawlow")])       # The low range for raw data vs modeled result graph.
allTogether = as.numeric(config$data[as.character(config$tag) == as.character("together")])        # Whether to have all individual stage graphs be on one figure or to use one figure per graph.
combined = as.numeric(config$data[as.character(config$tag) == as.character("combined")])           # Whether the stages should be graphed against the data points on a single figure.
stageNames = config$data[as.character(config$tag) == as.character("stage")]                        # A vector containing the names of each stage in the dataset.
imgWidth = as.numeric(config$data[as.character(config$tag) == as.character("width")])              # The width of the image.
combHigh = as.numeric(config$data[as.character(config$tag) == as.character("combHigh")])           # The high range for combined graph.
combLow = as.numeric(config$data[as.character(config$tag) == as.character("combLow")])             # The low range for combined graph.
imgHeight = as.numeric(config$data[as.character(config$tag) == as.character("height")])            # The height of the image.
stageMap = config$data[as.character(config$tag) == as.character("stageMap")]                       # Vector of strings mapping a stage's source index to a target index.

# Load the species and time information.
rawdata = read.table(toString(species[[1]]),header=FALSE,sep="\t")
rawmat = rawdata[,-1]    # Initialize the matrix of raw species information by reading the first input file.
timeData = rawdata[1]    # Load the date information. This assumes that the dates are in Julian format and are the same for every input dataset.
timeData = timeData[[1]]  

#--------------
# Process Input
#--------------

# Iterate through the rest of the species file (if they exist) and add their information to rawmat.
if(length(species) > 1) {
	for(i in 2:length(species)){
		rawdata = read.table(toString(species[[i]]),header=FALSE,sep="\t")
		rawmat = rawmat + rawdata[,-1]
	}
}

# Convert time (presumed to be in Julian format) to degree days.
degreedays = doubleSine(toString(weather[[1]]), 40, 4)
tempTime = 1:length(timeData)
for(i in 1:length(timeData)){
  tempTime[i] = degreedays[timeData[[i]]] # Map calculated degree day schedule to the dates associated with the input datasets.
}
timeData = tempTime # Final time vector.

# Create the final species data matrix by applying any user-supplied stage collapse operations.
stageData = collapse(rawmat,stageMap)
# Create the final vector of stage names to use in labels.
stageNames = collapseStageNames(stageNames,stageMap)

# Find the size and dimensions of the sample data.
n = rowSums(stageData) # Individual sample times.
r = ncol(stageData)    # Total number of developmental stages.

param = params

setwd(toString(saveLoc[[1]]))

#---------------
# Output graphs
#---------------

if(logPdf3D == 1) {
	png("figure3dPDF.png", width=imgWidth, height=imgHeight, pointsize=20)
	if((logPdf3DShigh < 1)||(logPdf3DShigh < logPdf3DSlow)) {
		logPdf3DShigh = length(timeData)
	}
	s = logPdf3DSlow:logPdf3DShigh
	timeT = logPdf3DTlow:logPdf3DThigh
	PDF = outer(s, timeT, findPDF)
	persp(s, timeT, PDF, xlab="s", ylab="t", zlab="Probability Density Function", main="Logistic PDF", theta = 30, phi = 30, border = NA, expand = 0.5, shade=0.1, box=TRUE, ticktype="detailed", col = "lightblue", lty=0)
	dev.off()
}

if(logPdf2D == 1) {
	png("figure2dPDF.png", width=imgWidth, height=imgHeight, pointsize=20)
	s = 0:((log2dsample + 1)*log2dinterval)
	timeT = (0:(log2dsample - 1))*log2dinterval + log2doffset
	if(timeT[1] <= 0) {
		timeT[1] = 1;
	}
	first = exp((s - timeT[1])/sqrt(param[length(param)]*timeT[1]))/(sqrt(param[length(param)]*timeT[1])*((1+exp((s - timeT[1])/sqrt(param[length(param)]*timeT[1])))^2))
	plot(s, s, ylim=c(0,first[which.max(first)] + first[which.max(first)]/10), xlab="s", ylab="Probability density function", main="Logistic PDF", type="l")
	if(log2dsample > 0){
		for(i in 1:length(timeT)){
			temp = exp((s - timeT[i])/sqrt(param[length(param)]*timeT[i]))/(sqrt(param[length(param)]*timeT[i])*((1+exp((s - timeT[i])/sqrt(param[length(param)]*timeT[i])))^2))
			x = which.max(temp)
			y = temp[x]
			text(x, y, paste("t=", timeT[i], sep=""), pos=3)
			lines(s, temp)
		}
	}
	dev.off()
}

if(exProp == 1) {
	png("figureExpected.png", width=imgWidth, height=imgHeight, pointsize=20)
	if((expProphigh == 0)||(expProphigh > timeData[length(timeData)])) {
		expProphigh = timeData[length(timeData)]
	}
	if(expProplow < 1) {
		expProplow = 1
	}
	rng = expProplow:expProphigh
	plot(rng, P_ij(param, 1, rng), type="l", xlab="Time (Degree Day)", ylab="Proportion in stage", ylim=c(0,1.05), main="Predicted Proportions", lty=0) #predicted line
	for(i in 1:r){
		temp = P_ij(param, i, rng)
		x = which.max(temp)
		y = temp[x]
		text(rng[x], y, i, pos=3)
		lines(rng, temp)
	}
	dev.off()
}

if(compareRaw == 1) {
	if((allTogether == 1)&&(r < 17)) {
		png("figureCompare.png", width=imgWidth, height=imgHeight, pointsize=20)
		if((compRawhigh == 0)||(compRawhigh > timeData[length(timeData)])) {
			compRawhigh = timeData[length(timeData)]
		}
		if(compRawlow < 1) {
			compRawlow = 1
		}
		if(r == 1) {
			layout(matrix(c(1),1,1))
		} else if (r == 2) {
			layout(matrix(c(1,2),1,2))
		} else if (r < 5) {
			layout(matrix(c(1,3, 2,4),2,2))
		} else if (r < 7) {
			layout(matrix(c(1,4, 2,5, 3,6),2,3))
		}  else if (r < 10) {
			layout(matrix(c(1,4,7, 2,5,8, 3,6,9),3,3))
		} else if (r < 13) {
			layout(matrix(c(1,5,9, 2,6,10, 3,7,11, 4,8,12),3,4))
		} else if (r < 17) {
			layout(matrix(c(1,5,9,13, 2,6,10,14, 3,7,11,15, 4,8,12,16),4,4))
		}
		rng = compRawlow:compRawhigh
		for(i in 1:r){
			adjusted = 1:nrow(stageData)
			for(j in 1:nrow(stageData)){
				adjusted[j] = stageData[j, i] / sum(stageData[j,]) # Scale the data. 
			}
			plot(rng, P_ij(param, i, rng), type="l", xlab="Degree Day", ylab="Proportion", ylim=c(0,1), main=stageNames[i]) #predicted line
			points(timeData, adjusted) # Actual data points.
		}
		dev.off()
	} else {
		if((compRawhigh == 0)||(compRawhigh > timeData[length(timeData)])) {
			compRawhigh = timeData[length(timeData)]
		}
		if(compRawlow < 1) {
			compRawlow = 1
		}
		rng = compRawlow:compRawhigh
		for(i in 1:r){
			l = paste("figureCompare", i, sep="")
			l = paste(l, ".png", sep="")
			png(l, width=imgWidth, height=imgHeight, pointsize=20)
			adjusted = 1:nrow(stageData)
			for(j in 1:nrow(stageData)){
				adjusted[j] = stageData[j, i] / sum(stageData[j,]) # Scale the data.
			}
			plot(rng, P_ij(param, i, rng), type="l", xlab="Degree Day", ylab="Proportion", ylim=c(0,1), main=stageNames[i]) #predicted line
			points(timeData, adjusted) # Actual data points.
			dev.off()
		}
	}
}

colorList = c("#4C8DFF","#EC1EFF","#FF3A6F","#EA7227","#FF8411","#9F91FF","#97C685","#FF7AF6","#3A6BFF","#4C1EA8","#FFB7BF","#05FF19","#C9FFA0","#51D3FF","#00FFD8","#FF77BB","#72A06E","#0F137A","#29CEB0","#F29191")

if(combined == 1) {
	plist = NULL
	png("figureCombined.png", width=imgWidth, height=imgHeight, pointsize=20)
	if((combHigh == 0)||(combHigh > timeData[length(timeData)])) {
		combHigh = timeData[length(timeData)]
	}
	if(combLow < 1) {
		combLow = 1
	}
	rng = combLow:combHigh
	plot(rng, P_ij(param, 1, rng), type="l", xlab="Time (Degree Day)", ylab="Proportion in stage", ylim=c(0,1.05), main="Sample vs. Prediction", lty=0)
	for(i in 1:r) {
		temp = P_ij(param, i, rng)
		lines(rng, temp,col=colorList[i],lwd=1)
			adjusted = 1:nrow(stageData)
			for(j in 1:nrow(stageData)){
				adjusted[j] = stageData[j, i] / sum(stageData[j,]) # Scale the data .
			}
			points(timeData, adjusted, pch=14+ (i %% 10),col=colorList[i],lwd=1) # Actual data points.
			plist = c(plist,14+ (i %% 10))
	}
	legend("bottomleft",legend=stageNames,pch=plist,col=colorList,lty=1,cex=0.7)
	dev.off()
}

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
# The mapping is done using two numbers, X and Y, in a string of the format "X:Y"
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
#   Original - Vector of strings holding the unmodified list of stage names
#   stageMap - vector of strings holding the map from one stage to another.
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

#
# Function which finds the time at which 100ξ% of plants have attained stage j
#
# Takes:
#    xi   - the ξ value to evaluate.
#    aVal - the value of a[j - 1].
#    v    - The variance.
#
# Returns: Time value Tau
#
getTau = function(xi, aVal, v) {
   ret = aVal
   logVal = log((1 - xi)/xi)
   logVal2 = logVal^2
   if (xi <= 0.5) {
      ret = aVal + (v/2) * logVal2 - 0.5 * logVal * sqrt(v * (4 * aVal + v * logVal2))
   } else if (xi > 0.5) {
      ret = aVal + (v/2) * logVal2 + 0.5 * logVal * sqrt(v * (4 * aVal + v * logVal2))
   }
   return(ret)
}

#------------
# Read Input
#------------

# The program takes one argument from the command line, the location of the config file to be used.
args = commandArgs(trailingOnly = TRUE)
settings = args[1]

config = read.table(settings,header=TRUE,stringsAsFactors=FALSE,sep="\t")
weather = config$data[as.character(config$tag) == as.character("weather")]                                 # Location of the weather file to use for graphing.
saveLoc = config$data[as.character(config$tag) == as.character("saveLoc")]                                 # Location the figures should be saved to so that the calling program can find them.
params = as.numeric(config$data[as.character(config$tag) == as.character("params")])                       # Vector holding the model parameters.
errorBars = as.numeric(config$data[as.character(config$tag) == as.character("error")])                     # Whether the script should put error bars on the forecasting figure.
fit = as.numeric(config$data[as.character(config$tag) == as.character("fit")])                             # Whether the script should create a goodness-of-fit figure.
fitAlone = as.numeric(config$data[as.character(config$tag) == as.character("fitAlone")])                   # Whether the script should create one goodness-of-fit figure per parameter or put all of them on the same figure.
fitHigh = as.numeric(config$data[as.character(config$tag) == as.character("fitHigh")])                     # The high bound for the fitness figure.
fitLow = as.numeric(config$data[as.character(config$tag) == as.character("fitLow")])                       # The low bound for the fitness figure.
stageNames = config$data[as.character(config$tag) == as.character("stage")]                                # A vector containing the names of each stage in the dataset.
prediction = as.numeric(config$data[as.character(config$tag) == as.character("predict")])                  # Whether the script should create a population forecast figure. 
predictHigh = as.numeric(config$data[as.character(config$tag) == as.character("predictHigh")])             # The high bound for the forecast figure.
predictLow = as.numeric(config$data[as.character(config$tag) == as.character("predictLow")])               # The low bound for the forecast figure.
proportionPercent = as.numeric(config$data[as.character(config$tag) == as.character("proportionPercent")]) # The targeted proportion in the forecast figure.
proportionStage = as.numeric(config$data[as.character(config$tag) == as.character("proportionStage")])     # The stage to use in the forecast figure.
tendencies =  as.numeric(config$data[as.character(config$tag) == as.character("tendencies")])              # Whether the script should create a figure showing the model's central tendencies.
imgWidth = as.numeric(config$data[as.character(config$tag) == as.character("width")])                      # The width in pixels of all created figures.
imgHeight = as.numeric(config$data[as.character(config$tag) == as.character("height")])                    # The height in pixels of all created figures.
stageMap = config$data[as.character(config$tag) == as.character("stageMap")]                               # Vector of strings mapping a stage's source index to a target index.
rawAstar = as.numeric(config$data[as.character(config$tag) == as.character("astar")])                      # Get the astar statistical information. Note: This needs to be further processed before it can be sorted
rawVstar = as.numeric(config$data[as.character(config$tag) == as.character("vstar")])                      # Get the vstar statistical information.

#--------------
# Process Input
#--------------

# Convert time (presumed to be in Julian format) to degree days.
degreedays = doubleSine(toString(weather[[1]]), 40, 4)

stageNames = collapseStageNames(stageNames,stageMap)
lenRawA = length(rawAstar)     # Holds the size of the rawAstar vector.
lenA = length(stageNames) - 1  # Holds the number of parameters stored in rawAstar.
lenParams = length(stageNames) # Holds the total number of parameters.

# Convert the astar vector into a 2D array
starMat = matrix(nrow=lenParams,ncol=lenRawA/lenA)
for(i in 1:lenA) {
   starMat[i,] = rawAstar[seq(i, lenRawA, lenA)]
}
# Add the vstar values to the final matrix.
starMat[lenParams,] = rawVstar

setwd(toString(saveLoc[[1]]))

#---------------
# Create Figures
#---------------

# Figure showing interception prediction line.
if(prediction == 1) {
	if(predictHigh == 0) {
		rng = predictLow:degreedays[length(degreedays)]
	} else {
		rng = predictLow:predictHigh
	}
	# Find the line of intersection.
	index = getTau(proportionPercent,params[proportionStage],params[length(params)])
	# Find the error bars.
	starIndex = 1:(lenRawA/lenA) # Holds all calculated index values for our error bars.
	# Find the proportions for each set of star values and store the calculated target point.
	for(i in 1:(lenRawA/lenA)) {
		starIndex[i] = getTau(proportionPercent,starMat[proportionStage,i],starMat[length(params),i])
	}
	starIndex = sort(starIndex)

	# Find the upper and lower ranges to draw our error bars.
	diffMin = index - quantile(starIndex, .025)
	diffMax = quantile(starIndex, .975) - index
	if(round(diffMin,1) == round(diffMax,1)) {
		err = paste("(+/-",round(diffMax,1),")",sep="")
	} else {
		err = paste("(+",round(diffMax,1),"/-",round(diffMin,1),")", sep="")
	}
	
	png("forecast.png", width=imgWidth, height=imgHeight, pointsize=20)
	plotTitle = paste("Time at which ", round(proportionPercent * 100,2),"% is in stage ", proportionStage," or less\nis ", round(index,1), " Degree Days ",err, sep="")
	plot(rng, P_ij(params, 1, rng), type="l", xlab="Time (Degree Day)", ylab="Proportion in stage ", ylim=c(0,1.05), main=plotTitle,lty=0)
	for(i in 1:length(stageNames)) {
		temp = P_ij(params, i, rng)
		x = which.max(temp)
		y = temp[x]
		text(rng[x], y, i, pos=3)
		lines(rng, temp)
	}
	abline(b=1,v=index)  # The forecast intercept.
	if(errorBars == 1) {
		abline(b=1,v=index - diffMin,lty=2)
		abline(b=1,v=index + diffMax,lty=2)
	}
	dev.off()
}

colorList = c("#8726FF","#FF565C","#19FBFF")

# Figures showing central tendency.
if(tendencies == 1) {
	ctMat = matrix(nrow=3,ncol=lenParams) # Holds the central tendency information for each parameter.
	for(i in 1:lenParams) {
		stageParams = sort(starMat[i,])
		ctMat[1,i] = params[i]
		ctMat[2,i] = mean(stageParams)
		ctMat[3,i] = median(stageParams)
	}
	buffer = (max(ctMat[,i]) - min(ctMat[,i])) * 2.5
	for(i in 1:lenParams) {
		if(i == lenParams) {
			plotTitle = "Variance Parameter Central Tendencies"
		} else {
			plotTitle = paste("Parameter ",i," Central Tendencies",sep="")
		}
		png(paste("tendencies",i,".png",sep=""), width=imgWidth, height=imgHeight, pointsize=20)
		barplot(ctMat[,i],beside=T,main=plotTitle,col=colorList,ylim=c((min(ctMat[,i]) - buffer),(max(ctMat[,i]) + buffer)))
		text(1, ctMat[1,i], ctMat[1,i], pos=3)
		text(2, ctMat[2,i], ctMat[2,i], pos=3)
		text(3, ctMat[3,i], ctMat[3,i], pos=3)
		if(ctMat[1,i] > ctMat[3,i]) {
			legend("topright",legend=c("Parameter","Mean","Median"),pch=15,col=colorList,lty=0,cex=1)
		} else {
			legend("topleft",legend=c("Parameter","Mean","Median"),pch=15,col=colorList,lty=0,cex=1)
		}
		dev.off()
	}
}

# Figures showing goodness-of-fit.
if(fit == 1) {
	if(fitHigh == 0) {
		rng = fitLow:degreedays[length(degreedays)]
	} else {
		rng = fitLow:fitHigh
	}
	if(fitAlone == 1) {
		for(i in 1:lenParams) {
			starVec = starMat[i,]
			starVec = sort(starVec)
			diffMin = quantile(starVec, .025)
			diffMax = quantile(starVec, .975)
			dm = params
			dm[i] = diffMin
			png(paste("Goodness-of-Fit",i,".png",sep=""),width=imgWidth, height=imgHeight, pointsize=20)
			plot(rng, P_ij(params, 1, rng), type="l", xlab="Time (Degree Day)", ylab="Proportion in stage ", ylim=c(0,1.05), main=paste("Goodness-Of-Fit\n",stageNames[i],sep=""),lty=0)	
			lines(rng, P_ij(params,i,rng))
			lines(rng, P_ij(dm,i,rng),lty=2)
			dm[i] = diffMax
			lines(rng, P_ij(dm,i,rng),lty=2)
			dev.off()
		}
	} else {
		png("Goodness-of-Fit.png",width=imgWidth, height=imgHeight, pointsize=20)
		plot(rng, P_ij(params, 1, rng), type="l", xlab="Time (Degree Day)", ylab="Proportion in stage ", ylim=c(0,1.05), main="Goodness-Of-Fit",lty=0)	
		for(i in 1:lenParams) {
			starVec = starMat[i,]
			starVec = sort(starVec)
			diffMin = quantile(starVec, .025)
			diffMax = quantile(starVec, .975)
			dm = params
			dm[i] = diffMin
			lines(rng, P_ij(params,i,rng))
			lines(rng, P_ij(dm,i,rng),lty=2)
			dm[i] = diffMax
			lines(rng, P_ij(dm,i,rng),lty=2)
		}
		dev.off()
	}
}
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
# Function which attempts to estimate the parameters.
#
# Takes:
#   dataMat â Matrix holding the phenology data.
#   timeT - Vector holding time data in degree days.
#   optMethod - which method to use in the call to optim.
#
# Returns: Vector with the estimated parameters.
#
autoEstimate = function(dataMat, timeT, optMethod){

   param0 = 1:ncol(dataMat)

   for(i in 1:(ncol(dataMat) - 1)){
      param0[i] = timeT[which.max(dataMat[,i])] # Get an initial estimate based on the time for the highest recorded level of a given developmental stage
   }
   param0[ncol(dataMat)] = param0[1] / 10 # Set BB to be 1/10 the value of the first parameter. No particular reason, just based on what worked for the initial prototype.
   for(i in 2:(length(param0) - 1)){      # The parameters can not have the same values. They must be positive numbers in ascending order.
      if(param0[i] <= param0[i-1]){
         param0[i] = param0[i-1] + 10
      }
   }

   param0=log(param0)
   PHENOL=optim(param0,negloglike,T=timeT,dataset=dataMat,method=optMethod)
   results=c(exp(PHENOL$par),PHENOL$val)
   param = exp(PHENOL$par) # Get the parameter values for the initial estimate.
   return(PHENOL)
}

#
# Function which attempts to estimate and then optimize the parameters.
#
# Takes:
#   dataMat â Matrix holding the phenology data.
#   timeT - Vector holding time data in degree days.
#
# Returns: Vector with the optimized parameters (if they could be found).
#
autoOptimize = function(dataMat, timeT){  
   param0 = 1:ncol(dataMat)

   for(i in 1:(ncol(dataMat) - 1)){
      param0[i] = timeT[which.max(dataMat[,i])] # Get an initial estimate based on the time for the highest recorded level of a given developmental stage.
   }
   param0[ncol(dataMat)] = param0[1] / 10 # Set BB to be 1/10 the value of the first parameter. No particular reason, just based on what worked for the initial prototype.
   for(i in 2:(length(param0) - 1)){      # The parameters can not have the same values. They must be positive numbers in ascending order.
      if(param0[i] <= param0[i-1]){
         param0[i] = param0[i-1] + 10
      }
   }

   param0=log(param0)
   PHENOL=optim(param0,maxL,dateT=timeT,X=dataMat,method="Nelder-Mead")
   results=c(exp(PHENOL$par),PHENOL$val)
   param = exp(PHENOL$par) # Get the parameter values for the initial estimate.

   lastEstimatedParam = exp(param0)
   lastParam = param
   delta = (1:length(param0))*10
   lastPHENOL = PHENOL
   # Keep increasing the starting parameters until the delta for the resulting estimation is less than 10 for each parameter.
   # Note: This will halt if it reaches a value for which it can produce no estimation.
   while(TRUE %in% (delta > 10)){
      newEstimatedParam = (1:length(param))*0
      for(i in 1:length(param)){
         if(delta[i] > 10){ # Only increase starting parameters when the delta is greater than 10.
	        if(i < length(param)){
	           newEstimatedParam[i] = lastEstimatedParam[i] + 50
	        }else{
		       newEstimatedParam[i] = lastEstimatedParam[i] + 10 # The B^2 value is increased by a smaller amount.
		    }
	     }else{
	        newEstimatedParam[i] = lastEstimatedParam[i]
	     }
      }
	  tPHENOL=optim(log(newEstimatedParam),maxL,dateT=timeT,X=dataMat,method="Nelder-Mead")
      if(tPHENOL$convergence == 0){
         tparam = exp(tPHENOL$par)
	     delta = abs(tparam - lastParam)
	     lastParam = tparam
	     lastEstimatedParam = newEstimatedParam
		 lastPHENOL = tPHENOL
      }else{
         delta = 0
      }
   }
   return(lastPHENOL)
}

#
# Function takes the array holding the per-stage phenology data and collapses stages in to 
# each other based on the user's inputs.
#
# The mapping is done using two numbers, X and Y, in a string of the format âX:Yâ
#	The X number is the original index of a given stage
#	The Y number is the index the stage information should be added to for the final output
#	If a given index is not collapsed into another one the map takes the form of âX:Xâ
#
# Takes:
#   Original â Array holding the unmodified phenology information
#   stageMap â vector of strings holding the map from one stage to another.
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
# Function to calculate accumulated degree days using the double sine method.
#
# Note: assumes input in Celsius
#
# Takes:
#   x â name of temperature data file
#   upper â high bound
#   lower â low bound
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
# Function which attempts to estimate the parameters from a set of starting parameters supplied by the user.
#
# Takes:
#   dataMat â Matrix holding the phenology data.
#   timeT - Vector holding time data in degree days.
#   optMethod - which method to use in the call to optim.
#   param0 â Vector holding the starting parameters
#
# Returns: Vector with the estimated parameters.
#
estimateWithParams = function(dataMat, timeT, optMethod, param0){

   param0=log(param0)
   PHENOL=optim(param0,negloglike,T=timeT,dataset=dataMat,method=optMethod)
   results=c(exp(PHENOL$par),PHENOL$val)
   param = exp(PHENOL$par) # Get the parameter values for the initial estimate.
   return(PHENOL)
}

#
# Function to be maximized. 
#
# Takes:
#   param â list of log(initial estimates)
#   dateT â time vector
#   X â phenology data matrix
#
# Returns: Maximized likelihood
#   
maxL = function(param,dateT,X) {
   A = exp(param)  
   BB = A[length(A)]
   q=nrow(X)
   r=ncol(X)
   P=matrix(0,q,r)
   for(i in 1:r){
      for(j in 1:q) {
	     if(i == 1){
            P[j,i]=1/(1+exp(-(A[1]-dateT[j])/sqrt(BB*dateT[j])))
         }
	     if(i == r){
            P[j,i]=1/(1+exp((A[r-1]-dateT[j])/sqrt(BB*dateT[j])))
		 }
         if((i>1)&(i<r)){
            P[j,i]=1/(1+exp(-(A[i]-dateT[j])/sqrt(BB*dateT[j])))- 1/(1+exp(-(A[i-1]-dateT[j])/sqrt(BB*dateT[j])))
         }
         if(P[j,i]<0.00000001){
			P[j,i]=0
		 }
      }
   }
   return(-sum(X*log(P)))
}

#
# Function to be maximized. 
#
# Takes:
#   params  â list of log(initial estimates)
#   T       â time vector
#   dataset â phenology data matrix
#
# Returns: log likelihood 
#
negloglike = function(params,T,dataset) {
  rows=nrow(dataset)
  cols=ncol(dataset)
  A=exp(params[1:(cols-1)])  
  V=exp(params[cols])
  P=matrix(0,rows,cols)
  for (i in 1:rows) {
    for (j in 1:cols) {
      if(j==1) P[i,j]=1/(1+exp(-(A[1]-T[i])/sqrt(V*T[i])))
      if(j==cols) P[i,j]=1/(1+exp((A[cols-1]-T[i])/sqrt(V*T[i])))
      if((j>1)&(j<cols)) P[i,j]=1/(1+exp(-(A[j]-T[i])/sqrt(V*T[i])))-
                        1/(1+exp(-(A[j-1]-T[i])/sqrt(V*T[i])))
      if(P[i,j]<0.00000001) P[i,j]=0.00000001
    }
  }
  return(-sum(dataset*log(P)))
}

#------------
# Read Input
#------------

# The program takes one argument from the command line, the location of the config file to be used.
args = commandArgs(trailingOnly = TRUE)
settings = args[1]

# Load the configuration file. This file contains all the input information needed for modeling.
config = read.table(settings,header=TRUE,stringsAsFactors=FALSE,sep="\t")
weather = config$data[as.character(config$tag) == as.character("weather")]      # Location of the weather file to use for modeling. 
species = config$data[as.character(config$tag) == as.character("species")]      # Vector holding the species files used for modeling.
saveLoc = config$data[as.character(config$tag) == as.character("saveLoc")]      # Location the model should be saved to so that the calling program can read it.
stageMap = config$data[as.character(config$tag) == as.character("stageMap")]    # Vector of strings mapping a stage's source index to a target index.
opt = config$data[as.character(config$tag) == as.character("optim")]            # Which method to use with the optimization function.
pars = as.numeric(config$data[as.character(config$tag) == as.character("par")]) # User-submitted initial parameters. This can be empty if the user wants the program to attempt an estimation on its own.

#--------------
# Process Input
#--------------

# Load the species and time information.
rawdata = read.table(toString(species[[1]]),header=FALSE,sep="\t")
rawmat = rawdata[,-1] # Initialize the matrix of raw species information by reading the first input file.
timeData = rawdata[1] # Load the date information. This assumes that the dates are in Julian format and are the same for every input dataset.
timeData = timeData[[1]]

# Iterate through the rest of the species file (if they exist) and add their information to rawmat.
if(length(species) > 1) {
	for(i in 2:length(species)) {
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

# Find the size and dimensions of the sample data.
#rows = rowSums(stageData) # Individual sample times.
cols = ncol(stageData)    # Total number of developmental stages.

#-----------------
# Calculate model
#-----------------
if(length(pars) > 0) {
	PHENOL = estimateWithParams(stageData, timeData, opt, pars)
} else {
	PHENOL = autoEstimate(stageData, timeData, opt)
}
results=c(exp(PHENOL$par),PHENOL$val)
logLikihood=sum(lfactorial(rowSums(stageData)))-sum(lfactorial(stageData))-results[cols+1]
AIC = -2 * logLikihood + 2 * length(results)

#-----------------
# Output the model
#-----------------

# Create string representing the output file name and location.
outpt = paste(toString(saveLoc[[1]]), "/modelOutput.xml", sep="")

# Create/truncate the file.
file.create(outpt)

# The data is passed back to the program as an XML file. Note that the tags are coded to display proper indenting.
# To see why extra tags are needed to wrap the <param></param> tags reference the external program documentation.
write("<models>", file = outpt, append = TRUE, sep = "\t")
write("\t<model>", file = outpt, append = TRUE, sep = "\t")
write(paste("\t\t<paramCount>",cols,"</paramCount>",sep=""), file = outpt, append = TRUE, sep = "\t")
# Output the calculated model parameters.
for(i in 1:cols){
	write("\t\t<param>", file = outpt, append = TRUE, sep = "\t")
	write(paste("\t\t\t<val>",results[i],"</val>",sep=""), file = outpt, append = TRUE, sep = "\t")
	write("\t\t</param>", file = outpt, append = TRUE, sep = "\t")
}
write(paste("\t\t<log>",logLikihood,"</log>",sep=""), file = outpt, append = TRUE, sep = "\t")
write(paste("\t\t<aic>",AIC,"</aic>",sep=""), file = outpt, append = TRUE, sep = "\t")
# Close the tags wrapping the model data.
write("\t</model>", file = outpt, append = TRUE, sep = "\t")
write("</models>", file = outpt, append = TRUE, sep = "\t")

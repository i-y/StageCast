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
# Function takes the array holding the per-stage phenology data and collapses stages in to 
# each other based on the user's inputs.
#
# The mapping is done using two numbers, X and Y, in a string of the format “X:Y”
#	The X number is the original index of a given stage
#	The Y number is the index the stage information should be added to for the final output
#	If a given index is not collapsed into another one the map takes the form of “X:X”
#
# Takes:
#   Original – Array holding the unmodified phenology information
#   stageMap – vector of strings holding the map from one stage to another.
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
#   x – name of temperature data file
#   upper – high bound
#   lower – low bound
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
# Function to be maximized. 
#
# Takes:
#   params  – list of log(initial estimates)
#   T       – time vector
#   dataset – phenology data matrix
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

# Load the configuration file. This file contains all the input information needed to find the desired statistics.
config = read.table(settings,header=TRUE,stringsAsFactors=FALSE,sep="\t")
weather = config$data[as.character(config$tag) == as.character("weather")]               # Location of the weather file to use. 
species = config$data[as.character(config$tag) == as.character("species")]               # Vector holding the species files to use.
saveLoc = config$data[as.character(config$tag) == as.character("saveLoc")]               # Location the statistics should be saved to so that the calling program can find them.
iterations = as.numeric(config$data[as.character(config$tag) == as.character("iter")])   # Number of bootstrap repetitions. Standard = 1000.
alpha = as.numeric(config$data[as.character(config$tag) == as.character("alpha")])       # Confidence intervals calculated at 100*(1-alpha) level. Standard = 0.05.
results = as.numeric(config$data[as.character(config$tag) == as.character("params")])    # Vector holding the model parameters.
AvalMat = as.numeric(config$data[as.character(config$tag) == as.character("AvalMat")])   # Whether the script should record matrix of A values (with high/lows).
AstarShow = as.numeric(config$data[as.character(config$tag) == as.character("Astar")])   # Whether the script should record the A star matrix.
VstarShow = as.numeric(config$data[as.character(config$tag) == as.character("Vstar")])   # Whether the script should record the V star matrix.
GGstarShow = as.numeric(config$data[as.character(config$tag) == as.character("GGstar")]) # Whether the script should record the GG star matrix.
stageMap = config$data[as.character(config$tag) == as.character("stageMap")]             # Vector of strings mapping a stage's source index to a target index.
opt = config$data[as.character(config$tag) == as.character("optim")]                     # Which method to use with the optimization function.

#--------------
# Process Input
#--------------

# Load the species and time information.
rawdata = read.table(toString(species[[1]]),header=FALSE,sep="\t")
rawmat = rawdata[,-1]    # Initialize the matrix of raw species information by reading the first input file.
timeData = rawdata[1]    # Load the date information. This assumes that the dates are in Julian format and are the same for every input dataset.
timeData = timeData[[1]]

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
# This will fail and/or produce irrational results if the size of the two inputs is not the same.
stageData = collapse(rawmat,stageMap)

# Find the size and dimensions of the sample data.
rows = nrow(stageData)    # Individual sample times.
cols = ncol(stageData)    # Total number of developmental stages.

stageData=stageData

theta0=log(c((max(timeData)/cols)*(1:(cols-1)),3))

A=results[1:(cols-1)]
V=results[cols]
logLikihood=sum(lfactorial(rowSums(stageData)))-sum(lfactorial(stageData))-results[cols+1]

fintime=800
inc=1
ntimes=fintime/inc+1
T=seq(0,fintime,inc)
P=matrix(0,ntimes,cols)
for (i in 1:cols) {
  if(i==1) P[,i]=1/(1+exp(-(A[1]-T)/sqrt(V*T)))
  if(i==cols) P[,i]=1/(1+exp((A[cols-1]-T)/sqrt(V*T)))
  if((i>1)&(i<cols)) P[,i]=1/(1+exp(-(A[i]-T)/sqrt(V*T)))-
                    1/(1+exp(-(A[i-1]-T)/sqrt(V*T)))
  P[,i]=P[,i]*(P[,i]>0.00000001)+ 0.00000001*(P[,i]<0.00000001)
}

#--------------------------------------
# Bootstrapping section
#--------------------------------------

rowTotals=rowSums(stageData)              # Vector of sample sizes from each sample.
Astar=matrix(0,iterations,cols-1)         # Will hold bootstrap values of the aj's.
AstartSorted=matrix(0,iterations,cols-1)  # Will hold sorted bootstrap values.
Vstar=numeric(iterations)                 # Will hold bootstrap values of v.
GGstar=numeric(iterations)                # Will hold bootstrap values of G-squared.
XXstar=numeric(iterations)                # Will hold bootstrap values of X-squared.
maxPJ=matrix(0,rows,cols)                 # Will hold ML estimates of the Pj(ti)'s.
Pjboot=matrix(0,rows,cols)                # Will hold bootstrap Pj(ti)'s.
Yboot=matrix(0,rows,cols)                 # Will hold bootstrap data sets (rewritten each simulation).
expected=matrix(0,rows,cols)              # Will hold expected values.
expectedBoot=matrix(0,rows,cols)          # Will hold bootstrap expected values.
aLow=numeric(cols-1)                      # Will hold lower bounds of CIs for the aj's.
aHigh=numeric(cols-1)                     # Will hold upper bounds of CIs for the aj's.


# Calculate the ML estimates of the pj(ti)'s.
for (j in 1:cols) {
  if(j==1) maxPJ[,j]=1/(1+exp(-(A[1]-timeData)/sqrt(V*timeData)))
  if(j==cols) maxPJ[,j]=1/(1+exp((A[cols-1]-timeData)/sqrt(V*timeData)))
  if((j>1)&(j<cols)) maxPJ[,j]=1/(1+exp(-(A[j]-timeData)/sqrt(V*timeData)))-
                    1/(1+exp(-(A[j-1]-timeData)/sqrt(V*timeData)))
  maxPJ[,j]=maxPJ[,j]*(maxPJ[,j]>0.00000001)+ 0.00000001*(maxPJ[,j]<0.00000001)
}

# Calculate the value of G-squared.
tempStageData=stageData+1*(stageData==0)
for (i in 1:cols) {
  expected[,i]=rowTotals*maxPJ[,i]
}
GG=2*sum(stageData*(log(tempStageData)-log(expected)))
XX=sum((stageData-expected)^2/expected)

# Generate bootstrap data and fit DK model.

for (i in 1:iterations) {

  # Generate bootstrap data.
  for (j in 1:rows) {
    Yboot[j,]=rmultinom(1,rowTotals[j],maxPJ[j,])
  }

  # Fit DK model.
  thetaboot0=log(c(A,V))
  PHENOLboot=optim(thetaboot0,negloglike,NULL,method=opt,timeData,Yboot)
  resultsboot=c(exp(PHENOLboot$par),PHENOLboot$val)
  Astar[i,]=resultsboot[1:(cols-1)]
  Vstar[i]=resultsboot[cols]

  # Calculate the bootstrap values of the pj(ti)'s.
  for (j in 1:cols) {
    if(j==1) Pjboot[,j]=1/(1+exp(-(Astar[i,1]-timeData)/sqrt(Vstar[i]*timeData)))
    if(j==cols) Pjboot[,j]=1/(1+exp((Astar[i,cols-1]-timeData)/sqrt(Vstar[i]*timeData)))
    if((j>1)&(j<cols)) Pjboot[,j]=1/(1+exp(-(Astar[i,j]-timeData)/sqrt(Vstar[i]*timeData)))-
                      1/(1+exp(-(Astar[i,j-1]-timeData)/sqrt(Vstar[i]*timeData)))
    Pjboot[,j]=Pjboot[,j]*(Pjboot[,j]>0.00000001)+
      0.00000001*(Pjboot[,j]<0.00000001)
  }

  # Calculate the value of G-squared.
  Yboot1=Yboot+1*(Yboot==0)
  for (j in 1:cols) {
    expectedBoot[,j]=rowTotals*Pjboot[,j]
  }
  GGstar[i]=2*sum(Yboot*(log(Yboot1)-log(expectedBoot)))
  XXstar[i]=sum((Yboot-expectedBoot)^2/expectedBoot)
}

# Calculate P-value for G-squared.
GGpval=sum(GG<=GGstar)/iterations                         # Bootstrap GG P-value.
GGpvalLow=GGpval-1.96*sqrt(GGpval*(1-GGpval)/iterations)  # Approx 95% CI for the bootstrap GG P-value.
GGpvalHigh=GGpval+1.96*sqrt(GGpval*(1-GGpval)/iterations)  
GGpvalCi=c(GGpvalLow,GGpvalHigh)                          # GG P-value confidence interval

# Calculate P-value for X-squared.
XXpval=sum(XX<=XXstar)/iterations                         # Bootstrap XX P-value.
XXpvalLow=XXpval-1.96*sqrt(XXpval*(1-XXpval)/iterations)  # Approx 95% CI for the bootstrap XX P-value.
XXpvalHigh=XXpval+1.96*sqrt(XXpval*(1-XXpval)/iterations)   
XXpvalCi=c(XXpvalLow,XXpvalHigh)                          # XX P-value confidence interval


# Calculate confidence intervals for parameters.
for (i in 1:(cols-1)) {
  AstartSorted[,i]=sort(Astar[,i]) # Sort the bootstrap parameters from smallest to largest, in preparation for calculating CIs.
}

aLow=AstartSorted[floor((alpha/2)*iterations),]      # 100*(alpha/2)th percentiles for lower ends of CIs.
aHigh=AstartSorted[ceiling((1-alpha/2)*iterations),] # 100*(1-alpha/2)th percentiles for higher end of CIs.

VstarSorted=sort(Vstar)
vLow=VstarSorted[floor((alpha/2)*iterations)]
vHigh=VstarSorted[ceiling((1-alpha/2)*iterations)]

# Create string representing the output file name and location.
outpt = paste(toString(saveLoc[[1]]), "/statOutput.xml", sep="")

# Create/truncate the file.
file.create(outpt)

write("<stats>", file = outpt, append = TRUE, sep = "\t")

write(paste("\t<iter>",iterations,"</iter>",sep=""),file = outpt, append = TRUE, sep = "\t")
write(paste("\t<alpha>",alpha,"</alpha>",sep=""),file = outpt, append = TRUE, sep = "\t")
write(paste("\t<stages>",ncol(Astar),"</stages>",sep=""),file = outpt, append = TRUE, sep = "\t")
write(paste("\t<paramCount>",length(A) + 1,"</paramCount>",sep=""),file = outpt, append = TRUE, sep = "\t")
write(paste("\t<gg>",GG,"</gg>",sep=""),file = outpt, append = TRUE, sep = "\t")
write(paste("\t<ggPval>",GGpval,"</ggPval>",sep=""),file = outpt, append = TRUE, sep = "\t")
write("\t<ggci>",file = outpt, append = TRUE, sep = "\t")
write(paste("\t\t<low>",GGpvalCi[1],"</low>",sep=""),file = outpt, append = TRUE, sep = "\t")
write(paste("\t\t<high>",GGpvalCi[2],"</high>",sep=""),file = outpt, append = TRUE, sep = "\t")
write("\t</ggci>",file = outpt, append = TRUE, sep = "\t")
write(paste("\t<optim>",opt,"</optim>",sep=""),file = outpt, append = TRUE, sep = "\t")
# Output the Pearson Chi-squared test information. 
write("\t<xx>", file = outpt, append = TRUE, sep = "\t")
write(paste("\t\t<stat>",XX,"</stat>",sep=""), file = outpt, append = TRUE, sep = "\t")
write(paste("\t\t<p>",XXpval,"</p>",sep=""), file = outpt, append = TRUE, sep = "\t")
write(paste("\t\t<low>",XXpvalLow,"</low>",sep=""), file = outpt, append = TRUE, sep = "\t")
write(paste("\t\t<high>",XXpvalHigh,"</high>",sep=""), file = outpt, append = TRUE, sep = "\t")
write("\t</xx>", file = outpt, append = TRUE, sep = "\t")
if(AvalMat == 1) {
	for(i in 1:length(A)){
		write("\t<aparam>",file = outpt, append = TRUE, sep = "\t")
		write(paste("\t\t<val>",A[i],"</val>",sep=""),file = outpt, append = TRUE, sep = "\t")
		write(paste("\t\t<low>",aLow[i],"</low>",sep=""),file = outpt, append = TRUE, sep = "\t")
		write(paste("\t\t<high>",aHigh[i],"</high>",sep=""),file = outpt, append = TRUE, sep = "\t")
		write("\t</aparam>",file = outpt, append = TRUE, sep = "\t")
	}
	write("\t<vparam>",file = outpt, append = TRUE, sep = "\t")
	write(paste("\t\t<val>",V,"</val>",sep=""),file = outpt, append = TRUE, sep = "\t")
	write(paste("\t\t<low>",vLow,"</low>",sep=""),file = outpt, append = TRUE, sep = "\t")
	write(paste("\t\t<high>",vHigh,"</high>",sep=""),file = outpt, append = TRUE, sep = "\t")
	write("\t</vparam>",file = outpt, append = TRUE, sep = "\t")
}
if(AstarShow == 1) {
	for(i in 1:nrow(Astar)) {
		write("\t<aStar>",,file = outpt, append = TRUE, sep = "\t")
		for(j in 1:ncol(Astar)) {
			write(paste("\t\t<val>",Astar[i,j],"</val>",sep=""),file = outpt, append = TRUE, sep = "\t")
		}
		write("\t</aStar>",,file = outpt, append = TRUE, sep = "\t")
	}
}
if(VstarShow == 1) {
	for(i in 1:length(Vstar)) {
		write(paste("\t<vStar>",Vstar[i],"</vStar>",sep=""),file = outpt, append = TRUE, sep = "\t")
	}
}
if(GGstarShow == 1) {
	for(i in 1:length(GGstar)) {
		write(paste("\t<ggStar>",GGstar[i],"</ggStar>",sep=""),file = outpt, append = TRUE, sep = "\t")
	}
}
write("</stats>", file = outpt, append = TRUE, sep = "\t")

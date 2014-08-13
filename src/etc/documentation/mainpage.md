About {#mainpage}
=============

[TOC]

# The System # {#system}

The StageCast system is an implementation of the general phenology modeling methods described by Dr. Brian Dennis and Dr. William Kemp[1][2][3]. With this algorithm, the growth of organisms which pass through discreet growth stages can be modeled as a function of environmental variables. Using past population data and current weather and population observations, a predictive model can be constructed. After model construction, a variety of further statistical operations can be performed on it to test the validity of the model as well as to gain useful insight [3]. For a more complete list of program functionality, please reference [4].

Predictive phenology modeling was originally developed in the context of insect development. It was first demonstrated as a method to predict the optimal time to deploy pesticides to control the western spruce budworm population. From that point, the model was generalized to address the development of grasshoppers on the steppe region of Montana. In addition to pest control, this model can also describe plant development. This program was developed specifically to model the development of the California almond crop and predict the best time for spraying or pollination.

This program replaces a more limited legacy program, DENNKEMP, which was created in the late 1980's. The aim of this replacement is to provide a more useful scientific platform for potential users. To do this, a program was created that strove to be capable, easy to use, and easily modifiable by the end users. The agricultural and scientific sectors are moving too quickly to make any accurate prediction of the market's needs in the future. For this reason, this program is designed to be modified and maintained by those who have a need for it, whether or not they have any formal training in computer science. 

# About this document # {#about}

This document covers the source code of the program as well as provides a general outline of the overall aims and structure of past and future program development. This is a technical document and will not provide guidance for how to use the program itself (i.e. using the user interface), and users looking for such guidance should consult the in-program help page. This document is concerned primarily with explaining the source code of the program; however, it may also be of use to users wishing to find the specifications for the various file formats used by the program. 

Like the program itself, this documentation is in the public domain and can be modified by the user as they see fit with no need to report such changes or ask permission. The source text of this documentation is in the source code itself and was turned in to the document you are currently reading using a free and open-source tool called doxygen. Most elements in this document are cross-referenced with the place they were declared in the source code, making it easier to find code or documentation of interest.

## Purpose ## {#purpose}

Writing code is an unavoidably technical undertaking; however, this document is designed to help the user learn the structure and function of the code, making it easier to perform changes. The structure of the program is based around discrete objects and packages of related objects. This design means that in most cases, any changes made to the code base will be limited to the specific area of code which was changed, rather than requiring edits throughout the code base to support the original change. Finding which section of code to change can be a challenge to someone coming to a new code base, and this is the problem that the documentation is here to try to address.

## Audience ## {#audience}

The audience of this documentation is expected to consist of a mix of technical and non-technical individuals who are interested it modifying the source code for their own use. Although this document makes every effort to explicitly and thoroughly outline the project in an accessible manner, it is not possible to provide a tutorial within the documentation sufficient to train a non-technical user to proficiency in programming. For this reason, it is recommended that non-technical users find a tutorial on Java programming to supplement this document. This document avoids technical jargon where possible but it is expected that the reader has at least some familiarity with common programming terms.

# Sections # {#sections}

The documentation is divided into three parts. The first part is the code-level documentation itself. This can be browsed via the index at the top of the page. Of the documentation relating to the project itself, it is divided into general and technical information. These two sections cover many of the same things but the general information is designed to be used as a quick reference whereas the technical information attempts to formally define program behavior and structure.
 
@ref docs "Documentation"

@subpage glos_file "Glossary of Terms"

# References # {#references}

|     |                                                                                                                                                                                                                     |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [1] | Dennis, Brian, William P. Kemp, and Roy C. Beckwith. \"Stochastic Model of Insect Phenology: Estimation and Testing.\" Environmental Entomology. 15.3 (1986): 540-546. Print.                                       |
| [2] | Dennis, Brian, and William P. Kemp. \"Further Statistical Inference Methods for a Stochastic Model of Insect Phenology.\" Environmental Entomology. 17.5 (1988): 887-893. Print.                                    |
| [3] | Kemp, William P., and Brian Dennis. \"Toward a General Model of Rangeland Grasshopper (Orthoptera: Acrididae) Phenology in the Steppe Region of Montana.\" Environmental Entomology. 20.6 (1991): 1504-1515. Print. |
| [4] | Yocum, Ian. <i>Reverse Engineering of Legacy Agricultural Phenology Modeling System</i>. Bachelor Thesis. University of North Dakota, Grand Forks, 2013.                                                            |

# Disclaimer # {#disclaimer}

This is a free and open source software package provided by the Agricultural Research Service of the United States Department of Agriculture (USDA ARS) in the hopes that it will advance the common interest. This disclaimer applies to all pages and files contained in or described by this documentation.

**ARS MAKES NO REPRESENTATION NOR EXTENDS ANY WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, OF 
MERCHANTABILITY OR FITNESS OF THE TECHNOLOGY IN THIS PROGRAM FOR ANY PARTICULAR PURPOSE, OR THAT 
THE USE OF THIS TECHNOLOGY WILL NOT INFRINGE ANY PATENT, COPYRIGHT, TRADEMARK, OR OTHER INTELLECTUAL 
PROPERTY RIGHTS, OR ANY OTHER EXPRESS OR IMPLIED WARRANTIES.**
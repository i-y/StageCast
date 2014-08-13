Save File Format {#save_file}
================

[TOC]

All input files given to the program will be parsed and saved in a format that makes it easier for the program to maintain the data and feed any necessary information to the R scripts used for statistical operations. All files will be stored as regular text files with names ending in ".txt" to make manual user interaction with the saved files as easy as possible. The specific location where these files are stored is user-configurable.

Organism Save Format {#organism_save}
====================

When the user inputs a organism data file to save they will be asked for a name. At the location specified by the user, a folder with the name of the data set will be created and all data files put into that location. The saved information will be stored in several files based on the nature of the input data. The first file created will be called `<data_name>.about.xml` and will store information relating to the data set. This file will contain the name of the data set, the name of the original source file, the time created, the update history, a manifest of organism files included in the data set, and the name of any related weather model given by the user. This document will use the XML mark up language to store this information in a manner easily read by the Java application.

After the about file is created, a series of organism files will be created. The input file is expected to have multiple organism stored in a single file; however, the program will put each organism in its own file. The files created will be named as `<data_name>.<organism_name>.txt` and will be stored in a tab-delineated column format.

An example of a single row from a organism with n life stages:

|      |                    |                    |     |                    |
|------|--------------------|--------------------|-----|--------------------|
| date | number in instar 1 | number in instar 2 | ... | number in instar n |

The organism name is dropped as unnecessary and at this point the date has been converted from its initial format into a Julian Date. This processing is designed to make the file as easy as possible for an R script to act on.

Weather Save Format {#weather_save}
===================

Weather files require less processing before use than the organism data but still go through a process similar to the one described above. The date is converted to Julian if necessary and the weather data converted to Celsius if the user has indicated that the input data is in Fahrenheit. The user will be prompted for a name for the data set and then allowed to record optional information such as the year the data is from, the location, associated organism data, and any notes.

The program will then store the information entered by the user in a file titled `<data_name>.about.xml` that is formatted in XML mark up language. The only other file in the weather file will be the document that holds the actual recordings. This file will be named `<data_name>.weather.txt` and will be stored in a tab-delineated column format.

An example of a single row from a valid weather file.

|      |      |     |
|------|------|-----|
| date | High | Low |

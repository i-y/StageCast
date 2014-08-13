Input File Formats  {#inpt_file}
==================


[TOC]

All input files must be in plain-text format such as could be opened by Notepad
on a Windows operating system. As long as the files correspond to the internal
format listed below, the file extension is not important. However, be aware that
some programs will automatically add formatting characters to their save files.
The program will not be able to read such a file until the extraneous information
is removed.

Organism Input Format {#organism_input}
=====================

The organism input files must be in a column delineated format.

An example of a single row from a organism with n life stages:

|      |          |                    |                    |     |                    |
|------|----------|--------------------|--------------------|-----|--------------------|
| date | organism | number in instar 1 | number in instar 2 | ... | number in instar n |

The delineation character between each column must be one of the following
and only one character can be used as a delineation character throughout the
whole input file.
 - a tab
 - a space, " "
 - a comma, ","

The date must be in a numeric format such as `dd/mm/yyyy`. The program
will accept dates with the days, month, or years in any order as long as the
same arrangement is used throughout the whole input file. The day, month,
and year information needs to be separated by one of the following characters
throughout the whole input file.
 - a forward slash, "/"
 - a dot, "."
 - a dash, "-"

The date could also be in Julian dating and so would consist of only one
number. The program will automatically pick up the use of Julian over calendar
dating but it must be consistent throughout the input file.

There can only be one organism entry per date and all organisms included
in the file must have the same number of instars.

Weather Input Format {#weather_input}
====================

The weather input file must be in a column delineated format and store readings in Celsius.

An example of a single row from a valid weather file.

|      |      |     |
|------|------|-----|
| date | High | Low |

The rules for what is a valid date format of column delineation character are the same as for the organism data.

Examples {#input_examples}
========

**Example 1**

A correctly formatted organism input file with the date in mm/dd/yyyy
format, using a period as the date element delineation character, and using a
comma as the column delineation character.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
2.11.2011,Padre,98,2,0,0,0,0,0,0
2.11.2011,California,10,90,0,0,0,0,0,0
2.11.2011,Sonora,3,27,68,2,0,0,0,0
2.11.2011,Butte,89,11,0,0,0,0,0,0
2.11.2011,Nonpareil,5,95,0,0,0,0,0,0
2.11.2011,Carmel,25,75,0,0,0,0,0,0
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Example 2**

A correctly formatted weather input file with the date in Julian format and using a space as the column delineation character.

~~~~~~~~~~~~~
1 49.3 43.0
2 53.4 36.5
3 52.7 32.2
4 43.7 36.1
5 41.5 39.6
6 40.6 37.2
7 41.7 38.1
8 41.7 38.5
9 45.5 22.8
10 46.0 33.3
~~~~~~~~~~~~~


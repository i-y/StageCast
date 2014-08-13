File Formats {#formats}
============

[TOC]

The program's operations center around files saved on the user's computer. These files could be the scientific data files given to the program as raw input, the parsed representation of this raw input, the scientific output of the program, or just information describing the program itself.

All of these files are described on this page. It is expected that the information of greatest importance for general use is the description of the acceptable input file formats. The rest of the files should be able to be handled transparently by the program. However, if the need arises the files are designed to be easy to modify by the user. These files are all stored as plain-text. This means that they can be opened and edited by text editors such as Window's Notepad or equivalent. For modifying the content fields themselves, please reference the appropriate loader classes.

# Input File Format # {#inpt}

The program is able to handle large input files which contain a mix of data from different organisms. It is then able to take this input data and save it as a database, ready for use in other areas of the program. The file specification tries to be reasonably flexible and accepts a variety of dating schemes and column-delineation methods. In most situations, a well-formed input file can be parsed and imported by the program automatically, without the need for user intervention.

There are, however, several caveats. The first is that, while the specifics are flexible, the over-all format is fixed. For example, the date must always be in the first column and files that do not follow this convention can not be imported at all. Further, it is expected that all organisms stored in an organism input file will have the same number of life stages. Importing from a document where the organisms do not have a uniform number of stages will result in undefined behavior which may cause the importation to fail or cause the statistical operations to output invalid information. Lastly, it should be pointed out that the ability of the program to automatically import the data is limited under some edge cases and the user should always review the program output before importing.

@note At the present time, each data file can encompass just one year and only one kind of data (organism or weather). This limitation may be removed in later updates.

## Organism Data Format ## {#inpt_organism}

**Valid File Format**

    Date | Organism Name | Growth Stage 1 | Growth Stage 2 | ... | Growth Stage N

Each column in the file must be separated by the same delineation character. The dates must be numeric and can either be Julian dates or any combination of dd/mm/yyyy. The date scheme must be consistent throughout.

@warning Although dates which use only two digits for the year are allowed, it is not recommended as it might lead to incorrect results in some cases.

**Valid Formatting Marks**
| Date       | Column    |
| :--------- | :-------- |
| slash '/'  | tab       |
| period '.' | space     |
| dash '-'   | comma ',' |

**Example File** 
|   Date     |   Name      | Stage 1 | Stage 2 | Stage 3 | Stage 4 |
|-----------:|------------:|--------:|--------:|--------:|--------:|
| 1.1.2012,  | Organism 1, |  0.9,   |  0.05,  |  0.04,  |  0.01   |
| 1.1.2012,  | Organism 2, |  0.95,  |  0.03,  |  0.02,  |  0      |
| 1.15.2012, | Organism 1, |  0.2,   |  0.2,   |  0.3,   |  0.1    |
| 1.15.2012, | Organism 2, |  0.5,   |  0.3,   |  0.15,  |  0.05   |
| 2.5.2012,  | Organism 1, |  0.05,  |  0.1,   |  0.25,  |  0.6    |
| 2.5.2012,  | Organism 2, |  0.0,   |  0.3,   |  0.5,   |  0.2    |
The date is in the format mm/dd/yyyy and uses the period formatting mark. 
The columns are delineated by a comma.
@note The order that the organism names are listed in does not have to be consistent; however, the dates must be listed in ascending order.

## Weather Data File ## {#inpt_weather}

Weather data files are formatted in a similar manner to the organism files and shares the valid formatting marks and styles. The primary difference is that there is two data columns, containing the high and low values for that date, and no name column. The organism input format can collect many related organisms together into a single file, only weather data from one sampling area is allowed per-file. Also, it maintains the limitation of the organism files of only being able to contain information from a single year.

@note Temperature recordings must be in Celsius.

**Valid File Format**

    Date | High | Low 

**Example File**
|    Date   | High | Low |
|:---------:|:----:|:---:|
| 15/3/1999 |  40  |  26 |
| 16/3/1999 |  35  |  28 |
| 17/3/1999 |  37  |  25 |
| 18/3/1999 |  41  |  30 |
The date is in the format dd/mm/yyyy and uses the forward slash formatting mark.
The columns are delineated by a space.

# Storage Formats # {#store}
The program stores information relating to its operations in different XML files. XML is a mark up language similar to HTML which can easily be read by the program while still being easy for the user to manually edit via a text editor such as Window's notepad or Gedit on Linux. The guiding design principle behind the design of the storage files is that they should list all information that either the program or the user might wish to have ready access to. This leads to files which are more verbose than strictly necessary, but it saves the program from having to dynamically find the needed information and run-time.

##Dataset Format ## {#dataset}
The program stores weather and organism data in a database. This database is a folder with the name of the organism or weather dataset. Inside the folder is a collection of files, where each individual organism listed in the input file is given its own named file containing its data and nothing else. These files are named using the convention of `<database_name>.<organism_name>.txt`. For weather datasets, there is only one such file, named `<database_name>.weather.txt`.

The program does not look at any of these files when asked to load the database. Instead, it reads a file named `<database_name>.about.txt`. This file stores all information about the database and its contents, including a list of file names and locations. Without this file, the program will not be able to identify the folder as a valid database. However, in the case of a missing or corrupted `<database_name>`.about.txt file, the program will attempt to construct a valid one by inspecting the rest of the files in the folder. This process is transparent to the user and does not require manual intervention in most cases.

**File Format**

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<about>
    <name>Dataset name</name>
    <date>creation date</date>
    <time>creation time</time>
    <location>.about.xml file location</location>
    <type>(Organism/Weather)</type>
    <stages>Number of Stages</stages>
    <stageNames>
        <nameVal>Stage 1 name</nameVal>
        <nameVal>Stage 2 name</nameVal>
        ...
        <nameVal>Stage N name</nameVal>
    </stageNames>
    <size>Number of Members</size>
    <members>
        <member>
            <location>Member 1 location</location>
            <name>Member 1 name</name>
        </member>
        <member>
            <location>Member 2 location</location>
            <name>Member 2 name</name>
        </member>
        ...
        <member>
            <location>Member N location</location>
            <name>Member N name</name>
        </member>
    </members>
</about>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

## Model Format ## {#model}

The program stores all models in a unified data file, called simply `models.xml`. Each individual model is contained between `<Model></Model>` tags. The model records information relating to when the model was created, what datasets were used, the individual organisms used, and the model elements themselves.

@warning Manual editing of this file should be carried out carefully as the program does not have as many recovery facilities for this file as it does for the other information files it creates and uses.

**File Format**

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<models>
    <model>
        <name>Model Name</name>
        <date>Creation date</date>
        <time>Creation time</time>
        <weatherName>Name of weather dataset</weatherName>
        <weatherLocation>Weather dataset location</weatherLocation>
        <organismName>Name of organism dataset</organismName>
        <organismLocation>Organism dataset location</organismLocation>
        <paramCount>Number of parameters</paramCount>
        <organismCount>Number of organisms</organismCount>
        <organismFile>First organism file location</organismFile>
        <organismFile>Second organism file location</organismFile>
        ...
        <organismFile>Nth organism file location</organismFile>
        <param>Parameter 1 value</param>
        <param>Parameter 2 value</param>
        ...
        <param>Parameter N value</param>
        <stageMap>F0:G0</stageMap>
        <stageMap>F1:G1</stageMap>
        ...
        <stageMap>FN:GN</stageMap>
        <log>Negative Log Likelihood</log>
        <aic>Akaike Information Criterion</aic>
        <optim>Optimization Method</optim>
    </model>
</models>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 ## Settings Format ## {#settings}

The program uses the file `settings.xml` to store user preferences as well as a list of previously loaded databases and models. As part of user preferences, it also records which datasets were loaded at the time the program was closed.

**File Format**

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<settings>
    <lastOrganism>Organism dataset</lastOrganism>
    <lastWeather>Weather dataset</lastWeather>
    <lastModel>Model name</lastModel>
    <organismImport>Organism load directory</organismImport>
    <weatherImport>Weather load directory</weatherImport>
    <organismSave>Organism save directory</organismSave>
    <weatherSave>Weather save directory</weatherSave>
    <modelLocation>Model file</modelLocation>
    <figureLocation>Figure save directory</figureLocation>
    <tempFigureLocation>Temporary figure output directory</tempFigureLocation>
    <organismList>
        <organism>
            <location>Organism dataset 1 location</location>
            <name>Organism dataset 1 name</name>
        </organism>
        <organism>
            <location>Organism dataset 2 location</location>
            <name>Organism dataset 2 name</name>
        </organism>
        ...
        <organism>
            <location>Organism dataset N location</location>
            <name>Organism dataset N name</name>
        </organism>
    </organismList>
    <weatherList>
        <weather>
            <location>Weather dataset 1 location</location>
            <name>Weather dataset 1 name</name>
        </weather>
        <weather>
            <location>Weather dataset 2 location</location>
            <name>Weather dataset 2 name</name>
        </weather>
        ...
        <weather>
            <location>Weather dataset N location</location>
            <name>Weather dataset N name</name>
        </weather>
    </weatherList>
</settings>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  
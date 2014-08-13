Data Storage File Formats {#data_file}
=========================

[TOC]

The program makes use of a markup method called XML to store information about files or datasets in a way that can easily be read in by the program. This is opposed to the simple formatting used in files designed to be read directly by R scripts.

Dataset {#data_save}
=======

This section covers the format used in the `<name>.about.txt` file in the saved datasets.

The format is:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 - The `<name>` tag stores the name of the dataset.
 - The `<date>` tag stores the date the dataset was created.
 - The `<time>` tag stores the time the dataset was created.
 - The `<location>` tag stores the location of the `*.about.xml` file. This is what is stored internally by the program.
 - The `<type>` tag stores whether the dataset stores organism or weather data.
 - The `<stages>` tag stores how many developmental stages the organisms stored in the dataset have. This is 0 for weather data.
 - The `<stageNames>` tags form a list of the stage names for the dataset.
    - The `<nameVal>` tag stores the name of one of the stages.
 - The `<size>` tag stores how many organisms are stored in the dataset. This is 1 for weather data.
 - The `<members>` tags form a list of the organisms stored in the dataset. This list has one entry for weather data, the link to the data file.
    - The `<member>` tag lists the organism name and file location for a given member.
        - The `<location>` tag stores the file path to the organism's datafile.
        - The `<name>` tag stores the name of the organism.

Model {#model_save}
=====

This section covers the format used to store information about user created models. It is also used by R to communicate back with the program. The script which calculates new models outputs a file in this format containing the calculated model which is then read back in to the program and added to the model database.

The format is:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 - The `<models>` tag wraps the list of all models.
 - The `<model>` wraps a single model data structure.
 - The `<name>` tag stores the name of the model.
 - The `<weatherName>` tag stores the name of the weather dataset used to generate the model.
 - The `<weatherLocation>` tag stores the location of the weather dataset used to generate the model.
 - The `<organismName>` tag stores the name of the organism dataset used to generate the model.
 - The `<organismLocation>` tag stores the location of the organism dataset used to generate the model.
 - The `<paramCount>` tag stores the number of parameters in the model.
 - The `<organismCount>` tag stores the number of organisms included in the model.
 - The `<organismFile>` tag stores the location of each organism data file.
 - The `<param>` tag stores the value of a parameter.
 - The `<stageMap>` tag is used to map each input stage on to an output stage.
 - The `<log>` tag stores the negative log likelihood value.
 - The `<aic>` tag stores the Akaike Information Criterion value for the model.
 - The `<optim>` tag records which optimization function was used to create the model.

Settings {#settings_save}
========

This section covers the format of the file the program uses to store internal information affecting the operation of the modeling program as a whole.

The format is:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
    <tooltips>(true/false)</tooltips>
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
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 - The `<settings>` tag is the root element of the file.
 - The `<lastOrganism>` tag stores the last organism dataset the user had open.
 - The `<lastWeather>` tag stores the last weather dataset the user had open.
 - The `<lastModel>` tag stores the last model the user had open. This is stored as the the name of the model, unlike the above two tags that store a link.
 - The `<organismImport>` tag stores the default directory the program will look in for organism files to import.
 - The `<weatherImport>` tag stores the default directory the program will look in for weather files to import.
 - The `<organismSave>` tag stores the default directory for the program to save organism datasets.
 - The `<weatherSave>` tag stores the default directory for the program to save weather datasets.
 - The `<modelLocation>` tag stores the location of the file containing saved models.
 - the `<tooltips>` tag stores whether the user has tooltips turned on or off.
 - The `<organismList>` tag forms a list of all organism datasets that have been loaded by the user.
    - The `<organism>` tag holds specified a single organism dataset.
        - The `<location>` tag specifies the location of the `*.about.xml` for that organism dataset.
        - The `<name>` tag specifies the name for that organism dataset.
 - The `<weatherList>` tag forms a list of all weather datasets that have been loaded by the user.
    - The `<weather>` tag holds specified a single weather dataset.
        - The `<location>` tag specifies the location of the `*.about.xml` for that weather dataset.
        - The `<name>` tag specifies the name for that weather dataset.

Statistics {#stat_out}
========

This section covers the xml representation of the StatsObject. This type of file is unique in that it is only output by an R script for the purpose of communicating information back to the Java program. The R script in question is that which runs further statistical operations on a collection of models and datasets after model creation. Some of the structure for this file might seem odd but it is designed to mimic the variable structure inside the R script.

@note If the program finds a statistics file in the “./Temp” folder at start up it will load and display that file to the user.

@note The `<aStar>`, `<vStar>`, and `<ggStar>` tags contain more than a thousand lines between them. These lines are represented here by a single entry per tag.

The format is:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<stats>
    <iter>Number of iterations</iter>
    <alpha>Alpha value</alpha>
    <stages>Organism stages</stages>
    <paramCount>Model parameters</paramCount>
    <gg>G-squared value</gg>
    <ggPval>G-squared p value</ggPval>
    <ggci>
        <low>G-squared low confidence interval value</low>
        <high>G-squared high confidence interval value</high>
    </ggci>
    <aparam>
        <val>Parameter 1 model value</val>
        <low>Parameter 1 low estimated value</low>
        <high>Parameter 1 high estimated value</high>
    </aparam>
    <aparam>
        <val>Parameter 2 model value</val>
        <low>Parameter 2 low estimated value</low>
        <high>Parameter 2 high estimated value</high>
    </aparam>
    ...
    <aparam>
        <val>Parameter N-1 model value</val>
        <low>Parameter N-1 low estimated value</low>
        <high>Parameter N-1 high estimated value</high>
    </aparam>
    <vparam>
        <val>Parameter N model value</val>
        <low>Parameter N low estimated value</low>
        <high>Parameter N high estimated value</high>
    </vparam>
    <aStar>Star values of parameters [1...N-1]</aStar>
    <vStar>Star values of parameter N</vStar>
    <ggStar>Star values of G-squared</ggStar>
</stats>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 - The `<stats>` tag wraps a statistics file.
 - The `<iter>` tag holds the number of iterations used to calculate the statistics.
 - The `<alpha>` tag holds the alpha value used in the statistical calculations.
 - The `<stages>` tag holds how many life stages the organisms have.
 - The `<paramCount>` tag holds how many parameters are in the model.
 - The `<gg>` tag holds the calculated G-squared value.
 - The `<ggPval>` tag holds the calculated G-squared p value.
 - The `<ggci>` tag holds the elements describing the G-squared confidence interval.
    - The `<low>` tag holds the low value for the G-squared confidence interval.
    - The `<high>` tag holds the high value for the G-squared confidence interval.
 - The `<aparam>` tag holds a parameter whose number is in the range [1...N-1]
    - The `<val>` tag holds the model parameter.
    - The `<low>` tag holds the estimated low parameter value.
    - The `<high>` tag holds the estimated high parameter value.
 - The `<vparam>` tag holds parameter N
    - The `<val>` tag holds the model parameter.
    - The `<low>` tag holds the estimated low parameter value.
    - The `<high>` tag holds the estimated high parameter value.
 - The `<aStar>` tag holds information output by the statistical calculations but not currently used by the main program.
 - The `<vStar>` tag holds information output by the statistical calculations but not currently used by the main program.
 - The `<ggStar>` tag holds information output by the statistical calculations but not currently used by the main program.
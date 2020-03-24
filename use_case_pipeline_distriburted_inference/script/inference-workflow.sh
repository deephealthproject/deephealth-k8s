#!/bin/bash

clear
echo "
  ____               ,                _      _         _                     _    _   _               
 (|   \             /|   |           | |    | |    o  (_|   |   |_/         | |  | | | |              
  |    | _   _    _  |___|  _   __,  | |_|_ | |         |   |   | __   ,_   | |  | | | |  __          
 _|    ||/  |/  |/ \_|   |\|/  /  |  |/  |  |/ \        |   |   |/  \_/  |  |/_) |/  |/  /  \_|  |  |_
(/\___/ |__/|__/|__/ |   |/|__/\_/|_/|__/|_/|   |_/o     \_/ \_/ \__/    |_/| \_/|__/|__/\__/  \/ \/  
               /|                                                                |\                   
               \|                                                                |/                   
"
echo ""

# read variables from config file
. inference-workflow.config
cd ..

# Split files
echo ""
echo "Kubernetes: "
cat 02-dh-job-splityaml.yaml | sed "s|\$PARTNUMBER|${partnumber}|g" | sed "s|\$PODINPUT|${pinput}|g" | sed "s|\$PODOUTPUT|${poutput}|g" | kubectl apply -f -

COND="Not Complete"

echo ""
echo "Creating $partnumber partitions from $pinput" 
while [ "$COND" != "Complete" ]; do
   COND=$(kubectl get job dh-job-splityaml -o jsonpath='{.status.conditions[*].type}')
   printf "."
done
echo "Done!"
echo ""

echo "Run jobs for each partitions of dataset"
partsubtractone=$((partnumber-1))
originalfilename=${pinput##*/}
prefix='part'
for i in $(seq -f "%03g" 0 $partsubtractone)
do 
   name=(part-$i-$originalfilename)
   cat 03-dh-job-inference.yaml | sed "s/\$PART/${part-$i}/" | sed "s/\$FILENAME/${name}/" | sed "s/\$NUMBER/${parnumber}/" | kubectl apply -f - | printf "" 
done
echo "Done!"

echo ""
echo "Worflows running!"
echo ""
echo "Use [kubectl get job -n dh] to check the status of the jobs."
echo "Use [kubectl get pod -n dh] to check the status of the  for each pod by job."
echo "Use [kubectl logs "POD_NAME" -n dh] to check the status of the logs for each job."
echo ""
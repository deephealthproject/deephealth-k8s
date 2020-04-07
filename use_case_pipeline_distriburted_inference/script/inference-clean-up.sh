#!/bin/bash

clear
echo "
  ____               ,                _      _          ___  _                                   
 (|   \             /|   |           | |    | |    o   / (_)| |                                  
  |    | _   _    _  |___|  _   __,  | |_|_ | |       |     | |  _   __,   _  _               _  
 _|    ||/  |/  |/ \_|   |\|/  /  |  |/  |  |/ \      |     |/  |/  /  |  / |/ |-----|   |  |/ \_
(/\___/ |__/|__/|__/ |   |/|__/\_/|_/|__/|_/|   |_/o   \___/|__/|__/\_/|_/  |  |_/    \_/|_/|__/ 
               /|                                                                          /|    
               \|                                                                          \|    
"
echo ""

# read variables from config file
. inference-workflow.config
cd ..

# remove split ds method
echo ""
echo "Kubernetes: "
echo ""
cat 03-dh-job-splityaml.yaml | sed "s|\$PARTNUMBER|${partnumber}|g" | sed "s|\$PODINPUT|${pinput}|g" | sed "s|\$PODOUTPUT|${poutput}|g" | kubectl delete -f -

# remove pods from each partition
partsubtractone=$((partnumber-1))
originalfilename=${pinput##*/}
prefix='part'
for i in $(seq -f "%03g" 0 $partsubtractone)
do 
   name=(part-$i-$originalfilename)
   cat 04-dh-job-inference.yaml | sed "s/\$PART/${part-$i}/" | sed "s/\$FILENAME/${name}/" | sed "s/\$NUMBER/${parnumber}/" | kubectl delete -f - | printf "" 
done
echo ""

# remove partition files
cat 05-dh-job-clean-up.yaml | sed "s|\$PODOUTPUT|${poutput}|g" | kubectl apply -f -
echo "Creating $partnumber partitions from $pinput" 
while [ "$COND" != "Complete" ]; do
   COND=$(kubectl get jobs dh-job-clean-up -o jsonpath='{.status.conditions[*].type}')
   printf "."
done
echo "Done!"
cat 05-dh-job-clean-up.yaml | sed "s|\$PODOUTPUT|${poutput}|g" | kubectl delete -f -

echo ""
echo "Clean-up completed!"

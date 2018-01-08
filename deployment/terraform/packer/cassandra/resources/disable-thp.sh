#!/bin/bash
# disables THP defrag for cassandra

if [ -d /sys/kernel/mm/transparent_hugepage ]; then
    sudo chmod a+w /sys/kernel/mm/transparent_hugepage/defrag
    sudo echo 'never' > /sys/kernel/mm/transparent_hugepage/defrag
else
    return 0
fi
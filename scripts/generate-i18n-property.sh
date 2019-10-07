#! /bin/sh

tmp1=$(mktemp)
tmp2=$(mktemp)

(grep -r I18nString.get * | grep -o -E '".*"' | sed 's/^"//' | sed 's/"$//g' | sed 's/ /_/g' | sed 's/\\/\\\\/g') > $tmp1
(grep -r I18nString.get * | grep -o -E '".*"' | sed 's/^"//' | sed 's/"$//g') > $tmp2
paste -d= $tmp1 $tmp2

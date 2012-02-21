#!/bin/sh

user=$1
pass=$2
outdir=$3


# windows XP, Firefox 10.
UA="Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.1.6) Gecko/20100101 Firefox/10.0"
curl="/usr/bin/curl --silent --cookie-jar ~/.advrider.cookies --user-agent \"$UA\" --referer http://lemonparty.org/"

$curl "http://advrider.com/forums/login.php?do=login&vb_login_username=${user}&vb_login_password=${pass}&submit=Login"


$curl "http://advrider.com/forums/external.php?type=RSS2&amp;forumids=52" > ${outdir}/bikes.xml
$curl "http://advrider.com/forums/external.php?type=RSS2&amp;forumids=53" > ${outdir}/parts.xml
$curl "http://advrider.com/forums/external.php?type=RSS2&amp;forumids=54" > ${outdir}/gear.xml
$curl "http://advrider.com/forums/external.php?type=RSS2&amp;forumids=55" > ${outdir}/other.xml



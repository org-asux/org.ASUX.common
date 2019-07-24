#!/bin/tcsh -f

###------------------------------
if (  !   $?ORGASUXFLDR ) then
        which asux >& /dev/null
        if ( $status == 0 ) then
                set ORGASUXFLDR=`which asux`
                set ORGASUXFLDR=$ORGASUXFLDR:h
                if ( "${ORGASUXFLDR}" == "." ) set ORGASUXFLDR=$cwd
                setenv ORGASUXFLDR "${ORGASUXFLDR}"
                echo "ORGASUXFLDR=$ORGASUXFLDR"
        else
                foreach FLDR ( ~/org.ASUX   ~/github/org.ASUX   ~/github.com/org.ASUX  /mnt/development/src/org.ASUX     /opt/org.ASUX  /tmp/org.ASUX  )
                        set ORIGPATH=( $path )
                        if ( -x "${FLDR}/asux" ) then
                                set ORGASUXFLDR="$FLDR"
                                set path=( $ORIGPATH "${ORGASUXFLDR}" )
                                rehash
                        endif
                end
                setenv ORGASUXFLDR "${ORGASUXFLDR}"
        endif
endif

###------------------------------
setenv ASUXORG_AWSNOTRELEVANT   ### Otherwise, user is FORCED to provide '--offline' cmdline flag _EVERYTIME_  :-(
source ${ORGASUXFLDR}/test/testAll-common.csh-source

###------------------------------
set PROJECTNAME=org.ASUX.common
set PROJECTPATH="${ORGASUXFLDR}/${PROJECTNAME}"

set TESTSRCFLDR=${PROJECTPATH}/test
chdir ${TESTSRCFLDR}
if ( "$VERBOSE" != "" ) pwd

set TEMPLATEFLDR=${TESTSRCFLDR}/outputs
set OUTPUTFLDR=/tmp/test-output-${PROJECTNAME}

\rm -rf ${OUTPUTFLDR}
mkdir -p ${OUTPUTFLDR}

###------------------------------
set TESTNUM=0

# 1
@ TESTNUM = $TESTNUM + 1
set OUTPFILE=${OUTPUTFLDR}/test-${TESTNUM}
echo $OUTPFILE
echo \
java -cp ${CLASSPATHCOMMON} org.ASUX.common.ScriptFileScanner ${VERBOSE} @script.txt
java -cp ${CLASSPATHCOMMON} org.ASUX.common.ScriptFileScanner ${VERBOSE} @script.txt >&! ${OUTPFILE}
diff ${OUTPFILE} ${TEMPLATEFLDR}/test-${TESTNUM}

###---------------------------------
# 2
@ TESTNUM = $TESTNUM + 1
set OUTPFILE=${OUTPUTFLDR}/test-${TESTNUM}
echo $OUTPFILE
echo \
java -cp ${CLASSPATHCOMMON} org.ASUX.common.ScriptFileScanner ${VERBOSE} 'aws.sdk --list-regions --double-quote; print -; aws.sdk --list-AZs us-east-1 --single-quote'
java -cp ${CLASSPATHCOMMON} org.ASUX.common.ScriptFileScanner ${VERBOSE} 'aws.sdk --list-regions --double-quote; print -; aws.sdk --list-AZs us-east-1 --single-quote' >&! ${OUTPFILE}
diff ${OUTPFILE} ${TEMPLATEFLDR}/test-${TESTNUM}

###---------------------------------
# 3
@ TESTNUM = $TESTNUM + 1
set OUTPFILE=${OUTPUTFLDR}/test-${TESTNUM}
echo $OUTPFILE
echo \
java -cp ${CLASSPATHCOMMON} org.ASUX.common.PropertiesFileScanner ${VERBOSE} @MyProperties.txt
java -cp ${CLASSPATHCOMMON} org.ASUX.common.PropertiesFileScanner ${VERBOSE} @MyProperties.txt >&! ${OUTPFILE}
diff ${OUTPFILE} ${TEMPLATEFLDR}/test-${TESTNUM}

###---------------------------------
# 4
@ TESTNUM = $TESTNUM + 1
set OUTPFILE=${OUTPUTFLDR}/test-${TESTNUM}
echo $OUTPFILE
echo \
java -cp ${CLASSPATHCOMMON} -DORGASUXHOME=${ORGASUXFLDR}  -DAWSCFNHOME=${ORGASUXFLDR}/AWS/CFN \
        org.ASUX.common.PropertiesFileScanner ${VERBOSE} @${ORGASUXFLDR}/AWS/CFN/config/DEFAULTS/Tags-fullstack-vpc-DEFAULTS.properties
java -cp ${CLASSPATHCOMMON} -DORGASUXHOME=${ORGASUXFLDR}  -DAWSCFNHOME=${ORGASUXFLDR}/AWS/CFN \
        org.ASUX.common.PropertiesFileScanner ${VERBOSE} @${ORGASUXFLDR}/AWS/CFN/config/DEFAULTS/Tags-fullstack-vpc-DEFAULTS.properties >&! ${OUTPFILE}
diff ${OUTPFILE} ${TEMPLATEFLDR}/test-${TESTNUM}



###---------------------------------
exit 0

#EoInfo


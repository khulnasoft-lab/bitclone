  $ ${TEST_SRCDIR}/java/com/khulnasoft/bitcode/bitcode help
  Usage: bitcode [options] CONFIG_PATH [SOURCE_REF]
    Options:
      --folder-dir
         Local directory to put the output of the transformation
      --gerrit-change-id
         ChangeId to use in the generated commit message
         Default: <empty string>
      --git-previous-ref
         Previous SHA-1 reference used for the migration.
         Default: <empty string>
      --help
         Shows this help text
         Default: false
      --work-dir
         Directory where all the transformations will be performed. By default a
         temporary directory.
      -v
         Verbose output.
         Default: false
  
  Example:
    bitcode myproject.bitcode origin/master

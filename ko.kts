/**
 * Sample (default) script that echoes its arguments.
 *
 * The ko.kts or ko.kt scripts are special catch all scripts that are executed
 * when a script matching the command cannot be found. In this case, the first
 * argument is the command.
 */

//DIR $HOME
//CMD hello Say hello
//CMD goodbye Say goodbye

println("ko args: ${args.joinToString(separator = " ")}")

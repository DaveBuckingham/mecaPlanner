#!/usr/bin/perl
use warnings;
use strict;

use feature qw(say);


#             AGENTS    ROOMS
my @TRIALS = (
              [2,        2],
#              [2,        3]
             );

my $TEMP_FILE = "temp.depl";

foreach my $trial (@TRIALS) {

    open(FH, '>', $TEMP_FILE) or die $!;

    my $num_agents = $$trial[0];
    my $num_rooms = $$trial[1];

    my @agents;
    my @secrets;
    my @rooms;

    foreach my $i (1..$num_agents) {
        push (@agents, "a$i");
        push (@secrets, "s$i()");
    }

    foreach my $i (1..$num_rooms) {
        push (@rooms, "r$i");
    }

   
    print FH "types{";
    print FH "Actor-Object,";
    print FH "Room-Object";
    print FH "}\n";
   
    print FH "objects{";
        foreach my $agent (@agents) {
            print FH "$agent-Actor,"
        }
    print FH "}\n";

    print FH "agents{";
        foreach my $agent (@agents) {
            print FH "$agent,";
        }
    print FH "}\n";
   
    print FH "fluents{";
        foreach my $secret (@secrets) {
            print FH "$secret, ";
        }
        print FH "at(Actor,Room)";
    print FH "}\n";

    print FH "initially{";
        foreach my $secret (@secrets) {
            print FH "$secret, ";
        }
        foreach my $agent (@agents) {
            print FH "at($agent, r1), ";
        }
    print FH "}\n";

    print FH "goals{";
    print FH "}\n";

    say FH "actions{";
        foreach my $i (1..$num_agents) {
            my $a = "a$i";
            my $s = "s$i()";
     say FH "    <?r - Room>tell(owner $a,\
                                 precondition at($a,?r),
                                 <?g-Actor>observes ?g if at(?g,?r),
                                 <?g-Actor>aware ?g if ~at(?g,?r), 
                                 announces $s)";
        }
        say FH "    <?a-Actor, ?f-Room, ?t-Room>move(owner ?a, precondition at(?a,?f), <?g-Actor>observes ?g, causes ~at(?a,?f), causes at(?a,?t))";
    print FH "}\n";

    `./mecad $TEMP_FILE`;

    close(FH);
}
    


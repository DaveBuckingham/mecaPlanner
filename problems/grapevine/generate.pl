#!/usr/bin/perl -w

use 5.010;
use warnings;
use strict;

my @NUM_ROOMS = (2,3);
my @NUM_AGENTS = (3,4);
my @MODAL_DEPTH = (1,2,3,4,5);

for my $num_rooms (@NUM_ROOMS) {
for my $num_agents (@NUM_AGENTS) {
for my $modal_depth (@MODAL_DEPTH) {

my ($i,$j,$k);

my $filename = "gossip${num_rooms}${num_agents}${modal_depth}.depl";

my @rooms;
$i = 0;
while ($i++ < $num_rooms) {
    push(@rooms, "r$i");
}

my @agents;
$i = 0;
while ($i++ < $num_agents) {
    push(@agents, "a$i");
}

my @secrets;
$i = 0;
while ($i++ < $num_agents) {
    push(@secrets, "s$i()");
}


open(FH, '>', $filename) or die $!;

say FH "// auto-generated gossip";
say FH "// rooms:  $num_rooms";
say FH "// agents: $num_agents";
say FH "// depth:  $modal_depth";


say FH "types{Actor-Object, Room-Object}";


say FH "objects{";
for (@agents) {
    say FH "    $_-Actor,";
}
for (@rooms) {
    say FH "    $_-Room,";
}
say FH "}";


print FH "agents{";
for (@agents) {
    print FH "$_,";
}
say FH "}";


say FH "fluents{";
print FH "    ";
for (@secrets) {
    print FH "$_,";
}
print FH "\n";
say FH "    at(Actor,Room)";
say FH "}";


say FH "start{";
print FH "    ";
for (@secrets) {
    print FH "$_,";
}
print FH "\n";
for (@agents) {
    say FH "    at($_,r1),";
}
for ($i = 1; $i <= $num_agents; $i++) {
    for ($j = 1; $j <= $num_agents; $j++) {
        print FH "    ";
        print FH $i == $j ? "B" : "?";
        print FH "[a$i](s$j),\n";
    }
}
say FH "}";


say FH "goals{";
for ($i = 1; $i <= $num_agents; $i++) {
    my $n = ($i % $num_agents) + 1;
    my $p = $i == 1 ? $num_agents : $i - 1;
    say FH "    B[a$i](s$n)";
    say FH "    !B[a$i](s$p)";
    if ($modal_depth > 1) {
        $j = 0;
        print FH "    ";
        while ($j < $modal_depth) {
            $k = (($i + $j - 1) % $num_agents) + 1;
            print FH "B[a$k](";
            $j++;
        }
        $k = (($i + $j - 1) % $num_agents) + 1;
        print FH "s$k()";
    }
    for ($j = 0; $j < $modal_depth; $j++) {
        print FH ")";
    }
    print FH "\n";
}
say FH "}";


say FH "actions{";

say FH "    <?a-Actor, ?f-Room, ?t-Room> move(";
say FH "        owner ?a,";
say FH "        <?x-Actor> observes ?x,";
say FH "        causes !at(?a,?f),";
say FH "        causes at(?a,?t)";
say FH "    )";

say FH "    <?a-Actor, ?f-Room, ?t-Room> move(";
say FH "        owner ?a,";
say FH "        <?x-Actor> observes ?x,";
say FH "        causes !at(?a,?f),";
say FH "        causes at(?a,?t)";
say FH "    )";


say FH "}";
 

close(FH);





}}}

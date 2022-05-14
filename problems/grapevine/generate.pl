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

my @agents;
$i = 0;
while ($i++ < $num_agents) {
    push(@agents, "a$i");
}



open(FH, '>', $filename) or die $!;

say FH "types{Actor-Object}";

say FH "objects{";
for (@agents) {
    say FH "    $_-Agent,";
}
say FH "}";




close(FH);





}}}

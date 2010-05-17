clear all;
close all;

%% ir

i = 85; %[deg]
Re = 6378;
GM = 398600.4;
h = 500;

a = Re + h;
P = 2*pi*sqrt(a^3/GM);
n = 0;
T1 = 0;
T2 = 0;

deltaN = 0.01:0.01:10;

%phi = atan((tan(90-deltaN./2))/cos(i));

deltaPhi = 180 - 2*atand((tand(90-deltaN/2))/cosd(i));

phiR = (T2-T1)*n + deltaPhi;

ir = acosd(cosd(i)^2+sind(i)^2*cosd(deltaN));

%% plot

figure1 = figure('Color',[1 1 1]); %varArea


% Create axes
axes('Parent',figure1);
box('on');
%hold on;

plot(deltaN,ir,'b-',deltaN,phiR,'r-.');
xlabel({'\DeltaN [deg]'});
ylabel({'Angle [deg]'});
h = legend('Relative inclination','Relative phase',2);
set(h,'Interpreter','none','Location','NorthWest')
hold off;

print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\relativeInc.png'

%% angular seperation

lambdaMin = 2*(asind(sind(phiR./2).*cosd(ir./2)));
lambdaMax = 2*(acosd(cosd(phiR./2).*cosd(ir./2)));

%% plot lambda

figure2 = figure('Color',[1 1 1]); %varArea


% Create axes
axes('Parent',figure2);
box('on');
%hold on;

plot(deltaN,lambdaMin,'b-',deltaN,lambdaMax,'r-.');
xlabel({'\DeltaN [deg]'});
ylabel({'Angle [deg]'});
h = legend('Min','Max',2);
set(h,'Interpreter','none','Location','NorthWest')
hold off;

print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\angularSeperation.png'
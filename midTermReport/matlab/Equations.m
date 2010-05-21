close all;
clear all;
clc;

%Constants
J2 = 0.001082645;
J3 = -0.000002546;
YearSid = 365.256363004;
DaySid = 86164.09053083288;
rEarth = 6378.13649;
omegaEarth = (2*pi)/DaySid;
nEarth = (2*pi)/(DaySid*YearSid);
mu = 398600.4418;

omega = 90*(pi/180);
e=-0.5*(J3/J2)*(rEarth/500)*sin(85*(pi/180))*sin(omega)

%Define mean motion
n = @(a) sqrt(mu./a.^3);

%Define p
p = @(a) a*(1-e.^2);

%Initial mean anomaly
M0 = @(a,i) 0.75*J2*(rEarth./p(a)).^2*sqrt(1-e^2).*(3*cos(i(a)).^2-1);

%Time derivative mean anomaly
Mdot = @(a,i) n(a).*(1+0.75*J2*(rEarth./p(a)).^2*sqrt(1-e^2).*(3*cos(i(a)).^2-1));

%Define the time rate of change of the argument of perigee
omegaDot = @(a,i,omega) ((3*J2.*n(a))/(1-(e^2))^2).* (rEarth./a).^2.* (1-1.25.*sin(i(a)).^2).* (1 + (J3/(2*J2*(1-e^2))).* (rEarth./a).* ((sin(i(a)).^2 -e^2.*cos(i(a)).^2)./sin(i(a)))* (sin(omega)/e));

%Calculate the nodal period
% Tomega = @(a,i,omega) ((2*pi)./(n(a)+M0(a,i)+omegaDot(a,i,omega)));

%Calculate OmegaDot
OmegaDot = @(a,i) (-3/2).*J2.*(rEarth./p(a)).^2.*cos(i(a)).*n(a);

%Determine the angle between ascending nodes sun synchronous
% deltaLambda = @(a,i,omega) (OmegaDot(a,i)-nEarth).*Tomega(a,i,omega);

%Plot i for a given a
a = 300+rEarth:1:500+rEarth;

%% Sun synchronous orbit

i = @(a) acos(-(2*nEarth*(a.^3.5)*(1-e^2)^2)/(3*J2*rEarth^2*sqrt(mu)));

figure1 = figure('Color',[1 1 1]);
axes('Parent',figure1);
box('on');
hold on;
plot(a,i(a)*(180/pi));
xlabel('Semimajor axis [km]');
ylabel('Inclination [deg]');

print -dpng 'D:\My Documents\Courses\AE3-001\Shared stuff\midTermReport\chapters\img\IncVsAlt';

%% Frozen orbit

%Repeart orbit
%KN = @(a,i,omega) (nEarth-omegaEarth)./(omegaDot(a,i(a),omega)+Mdot(a,i(a)));

i1 = 60 *(pi/180);
i2 = 65 *(pi/180);
i3 = 70 *(pi/180);
i4 = 75 *(pi/180);
i5 = 80 *(pi/180);
i6 = 85 *(pi/180);
i7 = 90 *(pi/180);
% i8 = 95 *(pi/180);
% i9 = 100 *(pi/180);

%asd;lf
eDot = @(a,i,omega) (3/2)*((J3*rEarth^3)./p(a).^3)*(1-e^2).*n(a).*sin(i).*cos(omega).*((5/4)*sin(i)^2-1);

%alsdjf
iDot = @(a,i,omega) (3/2)*((J3.*n(a))./(1-e^2)^3).*(rEarth./a).^3.*e.*cos(i).*cos(omega).*((5/4)*sin(i)^2-1);

figure2 = figure('Color',[1 1 1]);
axes('Parent',figure2);
box('on');
hold on;
plot(a,eDot(a,i1,omega)*DaySid,'b');
plot(a,eDot(a,i2,omega)*DaySid,'g');
plot(a,eDot(a,i3,omega)*DaySid,'r');
plot(a,eDot(a,i4,omega)*DaySid,'k');
plot(a,eDot(a,i5,omega)*DaySid,'c');
plot(a,eDot(a,i6,omega)*DaySid,'m');
plot(a,eDot(a,i7,omega)*DaySid,'y');
% plot(a,eDot(a,i8,omega)*DaySid,'b--');
% plot(a,eDot(a,i9,omega)*DaySid,'g--');
xlabel('Altitude [km]');
ylabel('Time rate of change of the eccentricity [-/day]');
legend('i=60','i=65','i=70','i=75','i=80','i=85','i=90');
print -dpng 'D:\My Documents\Courses\AE3-001\Shared stuff\midTermReport\chapters\img\AltVsEdot';

figure3 = figure('Color',[1 1 1]);
axes('Parent',figure3);
box('on');
hold on;
plot(a,(180/pi)*iDot(a,i1,omega)*DaySid,'b');
plot(a,(180/pi)*iDot(a,i2,omega)*DaySid,'g');
plot(a,(180/pi)*iDot(a,i3,omega)*DaySid,'r');
plot(a,(180/pi)*iDot(a,i4,omega)*DaySid,'k');
plot(a,(180/pi)*iDot(a,i5,omega)*DaySid,'c');
plot(a,(180/pi)*iDot(a,i6,omega)*DaySid,'m');
plot(a,(180/pi)*iDot(a,i7,omega)*DaySid,'y');
% plot(a,(180/pi)*iDot(a,i8,omega)*DaySid,'b--');
% plot(a,(180/pi)*iDot(a,i9,omega)*DaySid,'g--');
xlabel('Altitude [km]');
ylabel('Time rate of change of the inclination [deg/day]');
legend('i=60','i=65','i=70','i=75','i=80','i=85','i=90');

print -dpng 'D:\My Documents\Courses\AE3-001\Shared stuff\midTermReport\chapters\img\AltVsIdot';

%a;ldsjf;
e = @(a,i,omega) -0.5*(J3/J2).*(rEarth./a).*sin(i).*sin(omega);
p = @(a) a*(1-e.^2);
omegaDot = @(a,i,omega,e) ((3*J2.*n(a))./(1-e.^2).^2).* (rEarth./a).^2.* (1-(5/4).*sin(i).^2).*(1 + (J3./(2*J2*(1-e.^2))).* (rEarth./a).* ((sin(i).^2-(e.*cos(i)).^2)./sin(i)).* (sin(omega)./e));

figure4 = figure('Color',[1 1 1]);
axes('Parent',figure4);
box('on');
hold on;
plot(a-rEarth,(180/pi)*omegaDot(a,i1,omega,e(a,i1,omega))*DaySid,'b');
plot(a-rEarth,(180/pi)*omegaDot(a,i2,omega,e(a,i2,omega))*DaySid,'g');
plot(a-rEarth,(180/pi)*omegaDot(a,i3,omega,e(a,i3,omega))*DaySid,'r');
plot(a-rEarth,(180/pi)*omegaDot(a,i4,omega,e(a,i4,omega))*DaySid,'k');
plot(a-rEarth,(180/pi)*omegaDot(a,i5,omega,e(a,i5,omega))*DaySid,'c');
plot(a-rEarth,(180/pi)*omegaDot(a,i6,omega,e(a,i6,omega))*DaySid,'m');
plot(a-rEarth,(180/pi)*omegaDot(a,i7,omega,e(a,i7,omega))*DaySid,'y');
% plot(a-rEarth,(180/pi)*omegaDot(a,i8,omega)*DaySid,'b--');
% plot(a-rEarth,(180/pi)*omegaDot(a,i9,omega)*DaySid,'g--');
xlabel('Altitude [km]');
ylabel('Time rate of change of the argument of perigee [deg/day]');
legend('i=60','i=65','i=70','i=75','i=80','i=85','i=90');

print -dpng 'D:\My Documents\Courses\AE3-001\Shared stuff\midTermReport\chapters\img\AltVsOmdot';
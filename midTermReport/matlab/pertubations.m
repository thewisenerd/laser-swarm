clear all;
clc;
%function dV=deltaVrev(h, CDAM)
%Oribt perturbations calculations and graphs

%J2
J2 = 0.00108263;

%Solar radiation pressure

Areaarray = 0.1:0.1:20;
Area1 = 1;
Area2 = 10;
Area3 = 20;
Massarray = 1:1:150;
Mass1 = 75;
Mass2 = 250;
Mass3 = 500;


accelVarArea1 = -4.5e-8*Areaarray./Mass1;
accelVarArea2 = -4.5e-8*Areaarray./Mass2;
accelVarArea3 = -4.5e-8*Areaarray./Mass3;

accelVarMass1 = -4.5e-8*Area1./Massarray;
accelVarMass2 = -4.5e-8*Area2./Massarray;
accelVarMass3 = -4.5e-8*Area3./Massarray;

figure1 = figure('Color',[1 1 1]); %varArea


% Create axes
axes('Parent',figure1);
box('on');
hold on;

plot(Areaarray,accelVarArea1,'b-',Areaarray,accelVarArea2,'r-.',Areaarray,accelVarArea3,'k:');
xlabel({'Area normal to the Sun [m^2]'});
ylabel({'Acceleration [m/s^2]'});
h = legend('75 kg','250 kg', '500 kg',3);
set(h,'Interpreter','none')
hold off;

print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\solPressureVarArea.png'

figure2 = figure('Color',[1 1 1]); %varMass
% Create axes
axes('Parent',figure2);
box('on');
hold on;

plot(Massarray,accelVarMass1,'b-',Massarray,accelVarMass2,'r-.',Massarray,accelVarMass3,'k:');
xlabel({'Mass [kg]'});
ylabel({'Acceleration [m/s^2]'});
h = legend('1 m^2','10 m^2', '20 m^2',3);
set(h,'Interpreter','none')
hold off;

print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\solPressureVarMass.png'


% Drag Calculations

h = 300;
CDAM = 1/20;
GM = 3987600.4;
Re = 6378;
a=(Re+h);
Vc = sqrt(GM/a);
T = 2*pi*sqrt(a^3/GM);
year = 3600*24*365.25;
revs = year/T;

alt = [200:50:1000];
densityMin = [1.78e-10 3.35e-11 8.19e-12 2.34e-12 7.32e-13 2.47e-13 8.98e-14 3.63e-14 1.68e-14 9.14e-15 5.74e-15 3.99e-15 2.96e-15 2.28e-15 1.8e-15 1.44e-15 1.17e-15];
densityMean = [2.53000000000000e-10 6.24000000000000e-11 1.95000000000000e-11 6.98000000000000e-12 2.72000000000000e-12 1.13000000000000e-12 4.89000000000000e-13 2.21000000000000e-13 1.04000000000000e-13 5.15000000000000e-14 2.72000000000000e-14 1.55000000000000e-14 9.63000000000000e-15 6.47000000000000e-15 4.66000000000000e-15 3.54000000000000e-15 2.79000000000000e-15];
densityMax = [3.52e-10 1.06e-10 3.96e-11 1.66e-11 7.55e-12 3.61e-12 1.8e-12 9.25e-13 4.89e-13 2.64e-13 1.47e-13 8.37e-14 4.39e-14 3.0e-14 1.91e-14 1.27e-14 8.84e-15];
figure3 = figure('Color',[1 1 1]); %varMass
% Create axes
axes('Parent',figure3);
box('on');
hold on;

plot(densityMin,alt,'r-.');
plot(densityMax,alt,'k:');
plot(densityMean,alt);
xlabel({'Altitude [km]'});
ylabel({'Air Density [kg/m^3]'});
h = legend('Solar Minimum','Solar Maximum', 'Time Average',3);
set(h,'Interpreter','none','Location','NorthEast')
hold off;

print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\densityAltitude.png'

%for j = 1:20,
    

% dens=find(density==h);
% if (isempty(dens))
%     up = ceil(h/50)*50;
%     down = floor(h/50)*50;
%     rhoup=density(find(density==up),2);
%     rhodown=density(find(density==down),2);
%     rho = rhodown + (h-down)*(rhoup-rhodown)/(up-down);
% else
%     rho = density(dens,2);
% end
% dArev = -2*pi*CDAM*rho*a^2;
% dA = dArev*revs
% dVrev = pi*CDAM*rho*a*1000*Vc;
% dV = dVrev*revs
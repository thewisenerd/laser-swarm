clear all;
clc;
close all;


%%


%J2
J2 = 0.00108263;

%Solar radiation pressure

Areaarray = 0.01:0.1:10;
Area1 = 0.01;
Area2 = 2;
Area3 = 10;
Massarray = 5:1:500;
Mass1 = 5;
Mass2 = 250;
Mass3 = 500;


accelVarArea1 = -4.5e-8*Areaarray./Mass1;
accelVarArea2 = -4.5e-8*Areaarray./Mass2;
accelVarArea3 = -4.5e-8*Areaarray./Mass3;

accelVarMass1 = -4.5e-8*Area1./Massarray;
accelVarMass2 = -4.5e-8*Area2./Massarray;
accelVarMass3 = -4.5e-8*Area3./Massarray;

ratio = 1e-10:1e-6:5;
accelVarRatio = -4.5e-8.*ratio;

figure1 = figure('Color',[1 1 1]); %varArea


% Create axes
axes('Parent',figure1);
box('on');
%hold on;

semilogy(Areaarray,accelVarArea1,'b-',Areaarray,accelVarArea2,'r-.',Areaarray,accelVarArea3,'k:');
xlabel({'Area normal to the Sun [m^2]'});
ylabel({'Acceleration [m/s^2]'});
h = legend('5 kg','250 kg', '500 kg',3);
set(h,'Interpreter','none','Location','SouthEast')
hold off;

print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\solPressureVarArea.png'

figure2 = figure('Color',[1 1 1]); %varMass
% Create axes
axes('Parent',figure2);
box('on');
%hold on;

semilogy(Massarray,accelVarMass1,'b-',Massarray,accelVarMass2,'r-.',Massarray,accelVarMass3,'k:');
xlabel({'Mass [kg]'});
ylabel({'Acceleration [m/s^2]'});
h = legend('0.01 m^2','2 m^2', '10 m^2',3);
set(h,'Interpreter','none','Location','SouthEast')
hold off;

print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\solPressureVarMass.png'

figure21 = figure('Color',[1 1 1]); %varMass
% Create axes
axes('Parent',figure21);
box('on');
%hold on;

loglog(ratio,accelVarRatio);
xlabel({'Area/Mass ratio [-]'});
ylabel({'Acceleration [m/s^2]'});
hold off;

print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\solPressureVarRatio.png'

%%

%%
alt = [200:50:1000];
densityMin = [1.78e-10 3.35e-11 8.19e-12 2.34e-12 7.32e-13 2.47e-13 8.98e-14 3.63e-14 1.68e-14 9.14e-15 5.74e-15 3.99e-15 2.96e-15 2.28e-15 1.8e-15 1.44e-15 1.17e-15];
densityMean = [2.53000000000000e-10 6.24000000000000e-11 1.95000000000000e-11 6.98000000000000e-12 2.72000000000000e-12 1.13000000000000e-12 4.89000000000000e-13 2.21000000000000e-13 1.04000000000000e-13 5.15000000000000e-14 2.72000000000000e-14 1.55000000000000e-14 9.63000000000000e-15 6.47000000000000e-15 4.66000000000000e-15 3.54000000000000e-15 2.79000000000000e-15];
densityMax = [3.52e-10 1.06e-10 3.96e-11 1.66e-11 7.55e-12 3.61e-12 1.8e-12 9.25e-13 4.89e-13 2.64e-13 1.47e-13 8.37e-14 4.39e-14 3.0e-14 1.91e-14 1.27e-14 8.84e-15];

figure3 = figure('Color',[1 1 1]); %varMass

% Create axes

axes('Parent',figure3);
box('on');

%hold on;

semilogy(alt,densityMin,'r-.',alt,densityMax,'k:',alt,densityMean,'b-');
xlabel({'Altitude [km]'});
ylabel({'Air Density [kg/m^3]'});
hf = legend('Solar Minimum','Solar Maximum', 'Time Average',3);
set(hf,'Interpreter','none','Location','NorthEast')
hold off;

print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\densityAltitude.png'

%% Receiver

% Drag Calculations

Cd = 4;
Area = 0.01+0.2; %[m^2]
Mass = 10; %[kg]
CDAM = Cd*Area/Mass;

h = 300;

[revArray3,altitudeArray3]=orbitDecay(h,CDAM,1);
[revArray31,altitudeArray31]=orbitDecay(h,CDAM,2);
[revArray32,altitudeArray32]=orbitDecay(h,CDAM,3);

h = 400;

[revArray4,altitudeArray4]=orbitDecay(h,CDAM,1);
[revArray41,altitudeArray41]=orbitDecay(h,CDAM,2);
[revArray42,altitudeArray42]=orbitDecay(h,CDAM,3);

h = 500;

[revArray5,altitudeArray5]=orbitDecay(h,CDAM,1);
[revArray51,altitudeArray51]=orbitDecay(h,CDAM,2);
[revArray52,altitudeArray52]=orbitDecay(h,CDAM,3);

[altVmin,deltaVmin]= deltaV(CDAM,1);
[altVmean,deltaVmean]= deltaV(CDAM,2);
[altVmax,deltaVmax]= deltaV(CDAM,3);

deltaVtabMinRec = deltaVtab(CDAM,1);
deltaVtabMeanRec = deltaVtab(CDAM,2);
deltaVtabMaxRec = deltaVtab(CDAM,3);

figure4 = figure('Color',[1 1 1]); %varMass

% Create axes

axes('Parent',figure4);
box('on');

hold on;
grid on;
plot(revArray3,altitudeArray3,'b-');
plot(revArray4,altitudeArray4,'b-');
plot(revArray5,altitudeArray5,'b-');
plot(revArray31,altitudeArray31,'r-.');
plot(revArray41,altitudeArray41,'r-.');
plot(revArray51,altitudeArray51,'r-.');
plot(revArray32,altitudeArray32,'--k');
plot(revArray42,altitudeArray42,'--k');
plot(revArray52,altitudeArray52,'--k');
hold off;
axis([0 1825 200 575]);
ylabel({'Altitude [km]'});
xlabel({'Days After Launch'});
% hf = legend('Solar Minimum','Solar Maximum', 'Time Average',3);
% set(hf,'Interpreter','none','Location','NorthEast')


print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\orbitDecayRecieverMin.png'

figure41 = figure('Color',[1 1 1]); %varMass

% Create axes

axes('Parent',figure41);
box('on');

hold on;
grid on;
plot(altVmin,deltaVmin,'b-');
plot(altVmean,deltaVmean,'r-.');
plot(altVmax,deltaVmax,'--k');
hold off;
ylabel({'\DeltaV [m/s]'});
xlabel({'Altitude [km]'});
 hf = legend('Solar Minimum','Time Average', 'Solar Maximum',3);
 set(hf,'Interpreter','none','Location','NorthEast')


print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\deltaVReceiver.png'

%% Emitter
Cd = 4;
Area = 0.58+2; %[m^2]
Mass = 32; %[kg]
%CDAM = Cd*Area/Mass;
CDAM = 0.02;
h = 300;

[revArray3,altitudeArray3]=orbitDecay(h,CDAM,1);
[revArray31,altitudeArray31]=orbitDecay(h,CDAM,2);
[revArray32,altitudeArray32]=orbitDecay(h,CDAM,3);

h = 400;

[revArray4,altitudeArray4]=orbitDecay(h,CDAM,1);
[revArray41,altitudeArray41]=orbitDecay(h,CDAM,2);
[revArray42,altitudeArray42]=orbitDecay(h,CDAM,3);

h = 500;

[revArray5,altitudeArray5]=orbitDecay(h,CDAM,1);
[revArray51,altitudeArray51]=orbitDecay(h,CDAM,2);
[revArray52,altitudeArray52]=orbitDecay(h,CDAM,3);

[altVmin,deltaVmin]= deltaV(CDAM,1);
[altVmean,deltaVmean]= deltaV(CDAM,2);
[altVmax,deltaVmax]= deltaV(CDAM,3);

deltaVtabMinEmit = deltaVtab(CDAM,1);
deltaVtabMeanEmit = deltaVtab(CDAM,2);
deltaVtabMaxEmit = deltaVtab(CDAM,3);

figure4 = figure('Color',[1 1 1]); %varMass

% Create axes

axes('Parent',figure4);
box('on');

hold on;
grid on;
plot(revArray3,altitudeArray3,'b-');
plot(revArray4,altitudeArray4,'b-');
plot(revArray5,altitudeArray5,'b-');
plot(revArray31,altitudeArray31,'r-.');
plot(revArray41,altitudeArray41,'r-.');
plot(revArray51,altitudeArray51,'r-.');
plot(revArray32,altitudeArray32,'--k');
plot(revArray42,altitudeArray42,'--k');
plot(revArray52,altitudeArray52,'--k');
hold off;
axis([0 1825 200 575]);
ylabel({'Altitude [km]'});
xlabel({'Days After Launch'});
% hf = legend('Solar Minimum','Solar Maximum', 'Time Average',3);
% set(hf,'Interpreter','none','Location','NorthEast')


print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\orbitDecayEmitter.png'

figure51 = figure('Color',[1 1 1]); %varMass

% Create axes

axes('Parent',figure51);
box('on');

hold on;
grid on;
plot(altVmin,deltaVmin,'b-');
plot(altVmean,deltaVmean,'r-.');
plot(altVmax,deltaVmax,'--k');
hold off;
ylabel({'\DeltaV [m/s]'});
xlabel({'Altitude [km]'});
 hf = legend('Solar Minimum','Time Average', 'Solar Maximum',3);
 set(hf,'Interpreter','none','Location','NorthEast')


print -dpng 'C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\img\deltaVEmitter.png'

%% deltaV table
handle = fopen('C:\Users\Alex\Desktop\Laser Swarm\laser-swarm\midTermReport\chapters\deltaVtable.tex', 'w');
fprintf(handle, '\\begin{table}\n\\centering\n');
fprintf(handle, '\\begin{tabular}{ c | c | c | c | c | c | c | c | c | c }\n');
fprintf(handle, '& \\multicolumn{3}{|c|}{SOLAR MIN}&\\multicolumn{3}{|c|}{TIME AVERAGE}&\\multicolumn{3}{|c}{SOLAR MAX} \\\\ \\cline{2-10}\n');
fprintf(handle, 'Altitude & 300 & 400 & 500 & 300 & 400 & 500 & 300 & 400 & 500 \\\\ \\hline \\hline\n');
fprintf(handle, 'EMITTER & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f \\\\\n', deltaVtabMinEmit(1), deltaVtabMinEmit(2), deltaVtabMinEmit(3),deltaVtabMeanEmit(1), deltaVtabMeanEmit(2), deltaVtabMeanEmit(3), deltaVtabMaxEmit(1), deltaVtabMaxEmit(2), deltaVtabMaxEmit(3));
fprintf(handle, 'RECEIVER& %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f \\\\\n', deltaVtabMinRec(1), deltaVtabMinRec(2), deltaVtabMinRec(3),deltaVtabMeanRec(1), deltaVtabMeanRec(2), deltaVtabMeanRec(3), deltaVtabMaxRec(1), deltaVtabMaxRec(2), deltaVtabMaxRec(3));

fprintf(handle, '\\end{tabular}\n\\caption{Required $\\Delta$V for various orbit altitudes. In $m/s$.}\n\\label{table:deltaVTable}\n\\end{table}\n');
fclose(handle);

%% Earth radii
Re = 6378;
altid300 = (Re+300)/Re;
altid400 = (Re+400)/Re;
altid500 = (Re+500)/Re;

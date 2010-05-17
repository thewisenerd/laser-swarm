function [delta] = deltaVtab(CDAM,activity)

alt = [200:50:1000];
densityMin = [1.78e-10 3.35e-11 8.19e-12 2.34e-12 7.32e-13 2.47e-13 8.98e-14 3.63e-14 1.68e-14 9.14e-15 5.74e-15 3.99e-15 2.96e-15 2.28e-15 1.8e-15 1.44e-15 1.17e-15];
densityMean = [2.53000000000000e-10 6.24000000000000e-11 1.95000000000000e-11 6.98000000000000e-12 2.72000000000000e-12 1.13000000000000e-12 4.89000000000000e-13 2.21000000000000e-13 1.04000000000000e-13 5.15000000000000e-14 2.72000000000000e-14 1.55000000000000e-14 9.63000000000000e-15 6.47000000000000e-15 4.66000000000000e-15 3.54000000000000e-15 2.79000000000000e-15];
densityMax = [3.52e-10 1.06e-10 3.96e-11 1.66e-11 7.55e-12 3.61e-12 1.8e-12 9.25e-13 4.89e-13 2.64e-13 1.47e-13 8.37e-14 4.39e-14 3.0e-14 1.91e-14 1.27e-14 8.84e-15];

if (activity==1)
    density = densityMin;
else
    if (activity==2)
        density = densityMean;
    else
        density = densityMax;
    end
end
    

GM = 398600.5;
Re = 6378;

year = 3600*24*365.25;
day = 3600*24;

lifetime = 5; %[years]

%reset Initial Values
delta = [];

for(h=300:100:500),
    dens=find(alt==h);
    if (isempty(dens))
         up = ceil(h/50)*50;
         down = floor(h/50)*50;
         rhoup=density(find(alt==up));
         rhodown=density(find(alt==down));
         rho = rhodown + (h-down)*(rhoup-rhodown)/(up-down);
    else
         rho = density(dens);
    end
    V = sqrt(GM/(h+Re));%[km/s]
    P0 = 2*pi*sqrt((h+Re)^3/GM)/year; %[years]
    dVrev = pi*CDAM*rho*(h+Re)*1e6*V/P0;
    dV = dVrev*lifetime;
    %update values
    
    delta(end+1) = dV;   
end
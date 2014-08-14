clear all;
close all;
clc;

locations{1}='DESK';
locations{2}='BEDROOM';
locations{3}='DOORWAY';
locations{4}='BATHROOM';
locations{5}='KITCHEN';
locations{6}='DINING ROOM';
locations{7}='PIANO';
locations{8}='LIVING ROOM';

%  select * from locations where time>strftime('%s',datetime('now','-2 minutes'));
% Get unique ones
% select * from locations where time>strftime('%s',datetime('now','-5 minutes')) group by mac;
% % mksqlite('open','data.db')
% % test=mksqlite(['select rssi from (select rssi from locations where mac like ''00:1b:11:ef:df:32'' and room=5)'])
% % mksqlite('close')
% % mksqlite('open','data.db')
% % test=mksqlite(['select rssi from (select rssi from locations where mac like ''b8:3e:59:78:35:99'' and room!=7)'])
% % mksqlite('close')
% % histfit([test.rssi],7,'Normal')
% % values=[test.rssi];
% % [mu_est, sigma_est, w_est, counter, difference] = gaussian_mixture_model(values, 2, 1.0e-3);
% % mksqlite('open','data.db')
% % test=mksqlite(['select rssi from (select rssi from locations where mac like ''20:e5:2a:ac:b9:f4'' and room!=7)'])
% % mksqlite('close')
% % histfit([test.rssi],7,'Normal')
% % values=[test.rssi];
% % mu_est(1)=NaN
% % [mu_est, sigma_est, w_est, counter, difference] = gaussian_mixture_model(values, 2, 1.0e-3);
% % xx=-100:0.5:0;
% % p1_est = w_est(1) * norm_density(xx, mu_est(1), sigma_est(1));
% % p2_est = w_est(2) * norm_density(xx, mu_est(2), sigma_est(2));
% % [n,nx]=hist(values,8);
% % n=n/(sum(n)*mean(diff(nx)));
% % bar(nx,n); hold on;
% % plot(xx,p1_est,'r')
% % plot(xx,p2_est,'r')
% % xlabel('Signal dBm');ylabel('Probability')
% % legend('Measured values','Mixture model')
% % hold off;



mksqlite('open','data.db')
uniqueMACs=mksqlite(['select distinct mac from locations'])
mksqlite('close')

N{1}=zeros(length(locations),length(uniqueMACs),2)
N{2}=zeros(length(locations),length(uniqueMACs),2)
N{3}=zeros(length(locations),length(uniqueMACs),2)
mksqlite('open','data.db')
for i=1:length(locations)
    for j=1:length(uniqueMACs)
        foo=mksqlite(['select rssi from (select rssi from locations where mac like ''' char(uniqueMACs(j).mac) ''' and room=' num2str(i) ')']);
        if (size(foo,1)>0)
            mu_est(1)=NaN;
            tryNum = 0;
            while ((isnan(mu_est(1)) || isnan(mu_est(2))|| isnan(sigma_est(2))|| isnan(sigma_est(2))) && tryNum<10)
                [mu_est, sigma_est, w_est, counter, difference] = gaussian_mixture_model([foo.rssi], 2, 1.0e-3);
                tryNum = tryNum+1;
            end
            if (isnan(mu_est(1)) || isnan(mu_est(2))|| isnan(sigma_est(2))|| isnan(sigma_est(2)) && tryNum==10)
               mu_est(1)=mean([foo.rssi]);
               mu_est(2)=-90;
               sigma_est(1)=0.2;
               sigma_est(2)=200;
               w_est(1)=0.5;
               w_est(2)=0.01;
            end
            N{1}(i,j,:) = [mu_est(1) sigma_est(1)];
            N{2}(i,j,:) = [mu_est(2) sigma_est(2)];
            N{3}(i,j,:) = [w_est(1) w_est(2)];
        end
        foo=mksqlite(['select rssi from (select rssi from locations where mac not like ''' char(uniqueMACs(j).mac) ''' and room=' num2str(i) ')']);
        if (size(foo,1)>0)
            mu_est(1)=NaN;
            tryNum = 0;
            while ((isnan(mu_est(1)) || isnan(mu_est(2))|| isnan(sigma_est(2))|| isnan(sigma_est(2))) && tryNum<10)
                [mu_est, sigma_est, w_est, counter, difference] = gaussian_mixture_model([foo.rssi], 2, 1.0e-3);
                tryNum = tryNum+1;
            end
            if (isnan(mu_est(1)) || isnan(mu_est(2))|| isnan(sigma_est(2))|| isnan(sigma_est(2)) && tryNum==10)
               mu_est(1)=mean([foo.rssi]);
               mu_est(2)=-90;
               sigma_est(1)=0.2;
               sigma_est(2)=200;
               w_est(1)=0.5;
               w_est(2)=0.01;
            end
            antiN{1}(i,j,:) = [mu_est(1) sigma_est(1)];
            antiN{2}(i,j,:) = [mu_est(2) sigma_est(2)];
            antiN{3}(i,j,:) = [w_est(1) w_est(2)];
        end
    end
end
mksqlite('close')

for i=1:length(locations)
    for j=1:length(uniqueMACs)
        for k=1:2 %k=two gaussian
            if (isnan(N{k}(i,j,1)) || N{k}(i,j,1)>=0)
                N{k}(i,j,1)=-90;
            end
            if (isnan(N{k}(i,j,2)) || N{k}(i,j,1)==-90)
                N{k}(i,j,2)=200;
            elseif  (N{k}(i,j,2)<1e-4)
                N{k}(i,j,2)=0.05;
            end
        end
        k=3;
        if (isnan(N{k}(i,j,1)))
            N{k}(i,j,1)=0.00001;
        end
        if (isnan(N{k}(i,j,2)))
            N{k}(i,j,2)=0.00001;
        end
    end
end
for i=1:length(locations)
    for j=1:length(uniqueMACs)
        for k=1:2 %k=two gaussian
            if (isnan(antiN{k}(i,j,1)) || antiN{k}(i,j,1)>=0)
                antiN{k}(i,j,1)=-90;
            end
            if (isnan(antiN{k}(i,j,2)) || antiN{k}(i,j,1)==-90)
                antiN{k}(i,j,2)=200;
            elseif  (antiN{k}(i,j,2)<1e-4)
                antiN{k}(i,j,2)=0.05;
            end
        end
        k=3;
        if (isnan(antiN{k}(i,j,1)))
            antiN{k}(i,j,1)=0.00001;
        end
        if (isnan(antiN{k}(i,j,2)))
            antiN{k}(i,j,2)=0.00001;
        end

    end
end
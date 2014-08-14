%% SIMULATION: SINGLE READING
tests = [];
Pvalues{1} = [];
Pvalues{2}=[];
theDiff{1}=[];
theDiff{2}=[];
theThird{1}=[];
theThird{2}=[];
roomTest{1}=[];
roomTest{2}=[];
roomTestF{1}=[];
roomTestF{2}=[];
theRooms =[];
dropped = 0;
roomMatrix = zeros(length(locations),length(locations));
for ttt=1:3000;
% Get a random set of rows
mksqlite('open','data.db');
data=[];
roomId = randi(length(locations),1,1);
while (length(data)<1)
data=mksqlite(['select mac,rssi,room from (select mac,rssi,room from locations where room=' num2str(roomId) ' order by random() limit 10) group by mac']);
end
mksqlite('close');
W = ones(length(uniqueMACs),1).*-90;
for j=1:length(uniqueMACs)
    for i=1:length(data)
        if strcmp(char([data(i).mac]),char([uniqueMACs(j).mac]))
            % matched the MAC to something already loaded
            W(j)=data(i).rssi;
        end
    end
end
P_A = 1/length(locations);
P_notA = (length(locations)-1)/length(locations);
for i=1:length(locations)
    P_bayes(i)=0;
    for j=1:length(uniqueMACs)
        P_B_A=normpdf(W(j),N{1}(i,j,1),N{1}(i,j,2))*N{3}(i,j,1)+normpdf(W(j),N{2}(i,j,1),N{2}(i,j,2))*N{3}(i,j,2);
        P_B_notA=normpdf(W(j),antiN{1}(i,j,1),antiN{1}(i,j,2))*antiN{3}(i,j,1)+normpdf(W(j),antiN{2}(i,j,1),antiN{2}(i,j,2))*antiN{3}(i,j,2);
        P_bayes(i) = P_bayes(i) + (P_B_A*P_A) / (P_B_A*P_A + P_B_notA*P_notA);
    end
end
P_bayes = P_bayes/sum(P_bayes);
orderedP = flipud(sortrows([P_bayes' (1:length(locations))']));
[n,m]=max(P_bayes);
actualRoom = round(mean([data.room]));
if (actualRoom==m) 
%     disp(sprintf('Correct: %s (%2.1f)',locations{actualRoom},n*100));    
    Pvalues{1} = [Pvalues{1}; n*100];
    theDiff{1}=[theDiff{1} orderedP(1,1)/orderedP(2,1)];
    roomTest{1}=[roomTest{1},actualRoom];
    theThird{1}=[theThird{1};  orderedP(3,1)];
else
%     disp(sprintf('Incorrectly guessed %s (%2.1f) instead of %s (%2.1f)',locations{m},100*P_bayes(m),locations{actualRoom},100*P_bayes(actualRoom)));
    Pvalues{2} = [Pvalues{2}; n*100];
    theDiff{2}=[theDiff{2} orderedP(1,1)/orderedP(2,1)];
    roomTest{2}=[roomTest{2},actualRoom];
    theThird{2}=[theThird{2};  orderedP(3,1)];
end
theRooms = [theRooms; actualRoom m];

% cut offs for deciding

if (orderedP(1,1)/orderedP(2,1)>1.25 && orderedP(1,1)/orderedP(3,1)>1.5 && n*100>16)
    tests = [tests; actualRoom==m];
    if (actualRoom==m)
        roomTestF{1}=[roomTestF{1},actualRoom];
    else
        roomTestF{2}=[roomTestF{2},actualRoom];
    end
else
    % collect more data in real life
    dropped = dropped+1;
end
roomMatrix(actualRoom,m)=roomMatrix(actualRoom,m)+1;
end
disp(sprintf('Filtered accuracy: %2.1f%%, dropped: %2.1f%%, Raw accuracy: %2.1f%%',100*sum(tests)/length(tests),100*dropped/(length(Pvalues{1})+length(Pvalues{2})),100*length(Pvalues{1})/(length(Pvalues{1})+length(Pvalues{2}))))
disp(sprintf('\nUnfiltered individual accuracies:'))
for i=1:length(locations)
    numRight = length(find(roomTest{1}==i));
    numWrong = length(find(roomTest{2}==i));
    disp(sprintf('%s: %2.1f%% accurate',locations{i},100*numRight/(numRight+numWrong)))
end
disp(sprintf('\nFiltered individual accuracies:'))
for i=1:length(locations)
    numRight = length(find(roomTestF{1}==i));
    numWrong = length(find(roomTestF{2}==i));
    disp(sprintf('%s: %2.1f%% accurate',locations{i},100*numRight/(numRight+numWrong)))
end

figure(2)
plot3(theDiff{1},Pvalues{1},theThird{1},'o')
hold on;
plot3(theDiff{2},Pvalues{2},theThird{2},'or')
hold on;
view(3)

figure(3)
subplot(2,1,1)
plot(theDiff{1},Pvalues{1},'o',theDiff{2},Pvalues{2},'or')
legend('Correct','Incorrect')
xlabel('First/Second result');ylabel('First result probability')
subplot(2,1,2)
plot(theThird{1},Pvalues{1},'o',theThird{2},Pvalues{2},'or')
legend('Correct','Incorrect')
xlabel('First/Third result');ylabel('First result probability')
figure(4)
subplot(2,1,1)
nhist(roomTest,'number','legend',{'Correct','Incorect'})
title('Unfiltered')
subplot(2,1,2)
nhist(roomTestF,'number','legend',{'Correct','Incorect'})
title('Filtered')

figure(5)
imagesc(roomMatrix)
xlabel('Guess');ylabel('Actual')

figure(6)
subplot(1,2,1)
X1=hist(roomTest{1},1:8);
X2=hist(roomTest{2},1:8);
gg=[X1' X2'];
foo=gg;
gg(:,1)=100*gg(:,1)./sum(foo')';
gg(:,2)=100*gg(:,2)./sum(foo')';
bar(gg,'stacked')
title('Unfiltered')
legend('Correct','Incorret','location','SouthWest')
xlabel('Rooms')
ylabel('%%')
subplot(1,2,2)
X1=hist(roomTestF{1},1:8);
X2=hist(roomTestF{2},1:8);
gg=[X1' X2'];
foo=gg;
gg(:,1)=100*gg(:,1)./sum(foo')';
gg(:,2)=100*gg(:,2)./sum(foo')';
bar(gg,'stacked')
title('Filtered')
legend('Correct','Incorret','location','SouthWest')
xlabel('Rooms')
ylabel('%%')
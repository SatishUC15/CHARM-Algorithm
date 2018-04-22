# CHARM-Algorithm
An implementation of the CHARM algorithm for closed item set mining (Java). 

## Sample input
```
Items       Transactions
  A	      1,3,4,5
  D	      2,4,5,6
  T	      1,3,5,6 
  W	      1,2,3,4,5
  C	      1,2,3,4,5,6
  B	      7,8,9,11

```

## Sample output
Closed item sets:
```
  ACTW = [1, 3, 5] 
  ACW = [1, 3, 4, 5]
  B = [7, 8, 9, 11]
  C = [1, 2, 3, 4, 5, 6]
  CD = [2, 4, 5, 6]
  CDW = [2, 4, 5]
  CT = [1, 3, 5, 6]
  CW = [1, 2, 3, 4, 5]
```

## References
Zaki, Mohammed J., and Ching-Jui Hsiao. "CHARM: An efficient algorithm for closed itemset mining." Proceedings of the 2002 SIAM international conference on data mining. Society for Industrial and Applied Mathematics, 2002.

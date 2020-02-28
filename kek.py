def kek():
  for i in range(3):
    yield i

for j in (kek()):
  print(j)


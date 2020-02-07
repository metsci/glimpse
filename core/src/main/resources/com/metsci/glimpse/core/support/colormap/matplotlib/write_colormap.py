// simple script to generate colormap csv files

import matplotlib.pyplot as plt
import matplotlib as mpl
from matplotlib import cm
import numpy as np
import copy

def writeColormap( name, filename ):
    
    cmap = plt.get_cmap( name )
    cmaplist = [cmap(i) for i in range(cmap.N)]
    
    f = open( filename + '.csv', 'w')
    
    for l in cmaplist:
        f.write( str(l[0]) + ',' + str(l[1]) + ',' + str(l[2]) + '\n' )
    
    f.close( )

